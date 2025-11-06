import 'dart:async';
import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/screens/calendar/group_schedule_screen.dart';
import 'package:my_uz/screens/calendar/teacher_schedule_screen.dart';

class SearchScheduleScreen extends StatefulWidget {
  const SearchScheduleScreen({super.key});

  @override
  State<SearchScheduleScreen> createState() => _SearchScheduleScreenState();
}

class _SearchScheduleScreenState extends State<SearchScheduleScreen> {
  final TextEditingController _ctrl = TextEditingController();
  Timer? _debounce;
  bool _loading = false;
  List<Map<String, dynamic>> _results = [];
  Set<String> _favorites = {};

  @override
  void initState() {
    super.initState();
    _loadFavorites();
    _ctrl.addListener(() => setState(() {}));
  }

  Future<void> _loadFavorites() async {
    try {
      final favs = await ClassesRepository.loadFavorites();
      if (mounted) setState(() => _favorites = favs);
    } catch (_) {}
  }

  void _onQueryChanged(String q) {
    _debounce?.cancel();
    _debounce =
        Timer(const Duration(milliseconds: 250), () => _doSearch(q.trim()));
    setState(() {});
  }

  Future<void> _doSearch(String q) async {
    if (q.isEmpty) {
      if (mounted)
        setState(() {
          _results = [];
          _loading = false;
        });
      return;
    }

    if (mounted)
      setState(() {
        _loading = true;
      });

    final List<Map<String, dynamic>> out = [];
    try {
      final grows = await ClassesRepository.searchGroups(q, limit: 50);
      for (final m in grows) {
        final id = (m['id'] ?? '').toString();
        final code =
            (m['kod_grupy'] ?? m['kod'] ?? m['code'])?.toString() ?? '';
        final title = code.isNotEmpty ? code : id;
        out.add({'type': 'group', 'id': id, 'title': title, 'subtitle': ''});
      }
    } catch (e) {
      // ignore
    }

    try {
      final trows = await ClassesRepository.searchTeachers(q, limit: 50);
      for (final m in trows) {
        final id = (m['id'] ?? '').toString();
        final name = (m['nazwa'] ?? m['name'])?.toString() ?? '';
        out.add({
          'type': 'teacher',
          'id': id,
          'title': name.isNotEmpty ? name : id,
          'subtitle': ''
        });
      }
    } catch (_) {}

    final seen = <String>{};
    final deduped = <Map<String, dynamic>>[];
    for (final r in out) {
      final key = '${r['type']}:${r['id']}';
      if (seen.contains(key)) continue;
      seen.add(key);
      deduped.add(r);
    }

    if (mounted)
      setState(() {
        _results = deduped;
        _loading = false;
      });
  }

  bool _isFav(Map<String, dynamic> item) =>
      _favorites.contains('${item['type']}:${item['id']}');

  Future<void> _toggleFav(Map<String, dynamic> item) async {
    final key = '${item['type']}:${item['id']}';
    final label = (item['title'] ?? '').toString();
    try {
      await ClassesRepository.toggleFavorite(key,
          label: label.isEmpty ? null : label);
      await _loadFavorites();
    } catch (_) {}
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _ctrl.dispose();
    super.dispose();
  }

  Widget _buildListTile(Map<String, dynamic> item) {
    final type = (item['type'] ?? 'group').toString();
    final title = (item['title'] ?? '').toString();
    final subtitle = (item['subtitle'] ?? '').toString();
    final isFav = _isFav(item);
    final IconData iconData = type == 'teacher' ? MyUz.user_01 : MyUz.users_01;

    // Use theme primary color for favorite to match other screens
    final favColor = isFav ? Theme.of(context).primaryColor : const Color(0xFF9E9E9E);

    return ListTile(
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      leading: SizedBox(
        width: 48,
        child: Align(
          alignment: Alignment.center,
          child: Icon(iconData, size: 20, color: const Color(0xFF1D1B20)),
        ),
      ),
      title: Text(
        title,
        style: Theme.of(context).textTheme.bodyLarge,
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
      ),
      subtitle: subtitle.isNotEmpty
          ? Text(subtitle, style: Theme.of(context).textTheme.bodySmall, maxLines: 1, overflow: TextOverflow.ellipsis)
          : null,
      trailing: IconButton(
        icon: Icon(
          isFav ? Icons.favorite : MyUz.heart,
          color: favColor,
        ),
        onPressed: () => _toggleFav(item),
      ),
      onTap: () async {
        if (item['type'] == 'group') {
          final id = (item['id'] ?? '').toString();
          final code = (item['title'] ?? '').toString();
          final res = await Navigator.of(context)
              .push<Map<String, dynamic>?>(MaterialPageRoute(
            builder: (_) => GroupScheduleScreen(
                groupCode: code, groupId: id, groupName: code),
          ));
          if (!mounted) return;
          if (res != null && res['apply'] == true) {
            Navigator.of(context)
                .pop({'type': 'group', 'id': id, 'code': code});
          }
          return;
        }
        if (item['type'] == 'teacher') {
          final id = (item['id'] ?? '').toString();
          final name = (item['title'] ?? '').toString();
          final res = await Navigator.of(context)
              .push<Map<String, dynamic>?>(MaterialPageRoute(
            builder: (_) =>
                TeacherScheduleScreen(teacherId: id, teacherName: name),
          ));
          if (!mounted) return;
          if (res != null && res['apply'] == true) {
            Navigator.of(context)
                .pop({'type': 'teacher', 'id': id, 'name': name});
          }
          return;
        }
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(MyUz.chevron_left, color: Color(0xFF1D1B20)),
          onPressed: () => Navigator.of(context).pop(),
        ),
        title: SizedBox(
          height: 44,
          child: TextField(
            controller: _ctrl,
            autofocus: true,
            textInputAction: TextInputAction.search,
            onChanged: _onQueryChanged,
            decoration: InputDecoration(
              hintText: 'Szukaj planu grupy lub nauczyciela',
              prefixIcon: const Icon(MyUz.search),
              filled: true,
              fillColor: Theme.of(context).colorScheme.surface,
              border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: BorderSide.none),
              isDense: true,
              contentPadding:
              const EdgeInsets.symmetric(vertical: 12, horizontal: 12),
              suffixIcon: _ctrl.text.isNotEmpty
                  ? IconButton(
                  icon: const Icon(Icons.clear),
                  onPressed: () {
                    _ctrl.clear();
                    _doSearch('');
                  })
                  : null,
            ),
          ),
        ),
        centerTitle: false,
        actions: [],
      ),
      body: SafeArea(
        child: _loading
            ? const Center(child: CircularProgressIndicator())
            : (_results.isEmpty
            ? Center(
            child: Text(
                _ctrl.text.isEmpty
                    ? 'Wpisz frazę aby wyszukać'
                    : 'Brak wyników',
                style: TextStyle(color: Colors.grey.shade600)))
            : ListView.separated(
          itemCount: _results.length,
          separatorBuilder: (_, __) => const Divider(height: 1),
          itemBuilder: (context, i) => _buildListTile(_results[i]),
        )),
      ),
    );
  }
}