import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:convert';
import 'package:flutter/services.dart';

/// Placeholder ekranu planu nauczyciela.
/// Zwraca Map {'apply': true, 'id': id, 'name': name} jeśli użytkownik zatwierdzi.
class TeacherScheduleScreen extends StatefulWidget {
  final String teacherId;
  final String teacherName;
  final VoidCallback? onToggleFavorite;
  const TeacherScheduleScreen({super.key, required this.teacherId, required this.teacherName, this.onToggleFavorite});

  @override
  State<TeacherScheduleScreen> createState() => _TeacherScheduleScreenState();
}

class _TeacherScheduleScreenState extends State<TeacherScheduleScreen> {
  bool _isFavorite = false;
  bool _favAnimating = false;

  Future<void> _showTeacherInfo() async {
    final details = await ClassesRepository.getTeacherDetails(widget.teacherId);
    if (!mounted) return;
    final email = details?['email'] as String?;
    final instRaw = details?['instytut'] as String? ?? details?['institute'] as String?;
    final name = details?['nazwa'] as String? ?? widget.teacherName;
    // split institutes by comma/semicolon/newline if present
    final institutes = <String>[];
    if (instRaw != null && instRaw.trim().isNotEmpty) {
      institutes.addAll(instRaw.split(RegExp(r'[;,\n]')).map((s)=>s.trim()).where((s)=>s.isNotEmpty));
    }

    showDialog<void>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(name),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (email != null && email.isNotEmpty) ...[
              const Text('E-mail:'),
              Row(
                children: [
                  Expanded(child: SelectableText(email)),
                  IconButton(
                    icon: const Icon(Icons.copy, size: 20),
                    onPressed: () async {
                      await Clipboard.setData(ClipboardData(text: email));
                      Navigator.of(ctx).pop();
                      if (!mounted) return;
                      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Adres e-mail skopiowany')));
                    },
                    tooltip: 'Kopiuj e-mail',
                  )
                ],
              ),
            ],
            if (institutes.isNotEmpty) ...[
              const SizedBox(height: 8),
              const Text('Instytuty:'),
              const SizedBox(height: 6),
              for (final it in institutes) Text('- $it'),
            ],
            if ((email == null || email.isEmpty) && institutes.isEmpty)
              const Text('Brak dodatkowych informacji'),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.of(ctx).pop(), child: const Text('Zamknij')),
        ],
      ),
    );
  }

  Future<void> _loadFavoriteStatus() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final raw = prefs.getString('fav_plans');
      if (raw == null || raw.isEmpty) { setState(()=> _isFavorite = false); return; }
      final List<dynamic> l = jsonDecode(raw);
      final key = 'teacher:${widget.teacherId}';
      setState(() => _isFavorite = l.map((e)=>e.toString()).contains(key));
    } catch (_) { setState(()=> _isFavorite = false); }
  }

  Future<void> _toggleFavorite() async {
    if (widget.onToggleFavorite != null) {
      widget.onToggleFavorite!();
      setState(()=> _isFavorite = !_isFavorite);
      setState(() { _favAnimating = true; });
      Future.delayed(const Duration(milliseconds: 260), () { if (mounted) setState(() { _favAnimating = false; }); });
      return;
    }
    try {
      final prefs = await SharedPreferences.getInstance();
      final raw = prefs.getString('fav_plans');
      final List<String> list = raw == null || raw.isEmpty ? [] : List<String>.from(jsonDecode(raw).map((e)=>e.toString()));
      final key = 'teacher:${widget.teacherId}';
      if (list.contains(key)) { list.remove(key); setState(()=> _isFavorite = false); }
      else { list.add(key); setState(()=> _isFavorite = true); }
      await prefs.setString('fav_plans', jsonEncode(list));
      // update fav_labels map so Drawer shows readable name immediately
      try {
        final rawLabels = prefs.getString('fav_labels');
        Map<String,String> labels = {};
        if (rawLabels != null && rawLabels.isNotEmpty) {
          final dec = jsonDecode(rawLabels);
          if (dec is Map) dec.forEach((k,v){ if (k is String && v is String) labels[k]=v; });
        }
        if (list.contains(key)) {
          final label = widget.teacherName.isNotEmpty ? widget.teacherName : (await ClassesRepository.getTeacherDetails(widget.teacherId))?['nazwa'] as String? ?? widget.teacherId;
          if (label.isNotEmpty) labels[key] = label;
        } else {
          labels.remove(key);
        }
        await prefs.setString('fav_labels', jsonEncode(labels));
      } catch (_) {}
      setState(() { _favAnimating = true; });
      Future.delayed(const Duration(milliseconds: 260), () { if (mounted) setState(() { _favAnimating = false; }); });
      final snack = _isFavorite ? 'Dodano do ulubionych' : 'Usunięto z ulubionych';
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(snack)));
    } catch (_) {}
  }

  @override
  void initState(){
    super.initState();
    _loadFavoriteStatus();
  }

  @override
  Widget build(BuildContext context) {
    final favColor = _isFavorite ? (Theme.of(context).colorScheme.primary) : Theme.of(context).iconTheme.color;
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(MyUz.chevron_left, size: 24),
          onPressed: () => Navigator.of(context).pop(null),
        ),
        centerTitle: false,
        titleSpacing: 0,
        backgroundColor: Colors.white,
        foregroundColor: Theme.of(context).colorScheme.onSurface,
        elevation: 0,
        title: Align(
          alignment: Alignment.centerLeft,
          child: Padding(
            padding: const EdgeInsets.only(left:4.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Plan nauczyciela', style: Theme.of(context).textTheme.titleLarge),
                const SizedBox(height:2),
                Text(widget.teacherName.isNotEmpty ? widget.teacherName : 'Nauczyciel', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w600)),
              ],
            ),
          ),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.info_outline),
            onPressed: _showTeacherInfo,
            tooltip: 'Informacje o nauczycielu',
          ),
          Padding(
            padding: const EdgeInsets.only(right: 8.0),
            child: AnimatedScale(
              scale: _favAnimating ? 1.15 : 1.0,
              duration: const Duration(milliseconds: 220),
              curve: Curves.easeOutBack,
              child: IconButton(
                icon: Icon(_isFavorite ? Icons.favorite : Icons.favorite_border, color: favColor),
                onPressed: _toggleFavorite,
                tooltip: _isFavorite ? 'Usuń z ulubionych' : 'Dodaj do ulubionych',
              ),
            ),
          ),
          TextButton(onPressed: () => Navigator.of(context).pop(null), child: const Text('Anuluj')),
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            const Divider(height: 1, thickness: 1),
            Expanded(
              child: Container(
                color: Colors.white,
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 16),
                child: Column(
                  children: [
                    Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: const [
                        // Przykładowe podpowiedzi - dla nauczyciela mogą być puste
                      ],
                    ),
                    const SizedBox(height: 12),
                    Expanded(
                      child: Container(
                        decoration: BoxDecoration(
                          color: Colors.white,
                          border: Border.all(color: Theme.of(context).dividerColor, width: 0.5),
                        ),
                        child: CustomPaint(
                          painter: _GridPainter(),
                          child: const Center(child: Text('Widok planu (w budowie)', style: TextStyle(color: Colors.grey))),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const Divider(height: 1, thickness: 1),
            Padding(
              padding: const EdgeInsets.all(12.0),
              child: Row(
                children: [
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () => Navigator.of(context).pop(null),
                      child: const Text('Anuluj'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: () async {
                        try {
                          await ClassesRepository.setTeacherPrefsById(widget.teacherId);
                        } catch (_) {}
                        if (!mounted) return;
                        Navigator.of(context).pop({'apply': true, 'id': widget.teacherId, 'name': widget.teacherName});
                      },
                      child: const Text('Ustaw jako mój nauczyciel'),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _GridPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    // Colors.grey (0xFF9E9E9E) ~ RGB(158,158,158). Używamy Color.fromRGBO, by uniknąć deprecacji .withOpacity().
    final paint = Paint()..color = const Color.fromRGBO(158, 158, 158, 0.12)..strokeWidth = 0.5;
    final rows = 12;
    final cols = 7;
    final rowH = size.height / rows;
    final colW = size.width / cols;
    for (int i = 1; i < rows; i++) {
      final y = rowH * i;
      canvas.drawLine(Offset(0, y), Offset(size.width, y), paint);
    }
    for (int i = 1; i < cols; i++) {
      final x = colW * i;
      canvas.drawLine(Offset(x, 0), Offset(x, size.height), paint);
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
