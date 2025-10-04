import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:my_uz/screens/calendar/group_schedule_screen.dart'; // added: preview screen
import 'package:my_uz/screens/calendar/teacher_schedule_screen.dart';

/// Ekran: wyszukiwanie planu grupy lub nauczyciela.
class SearchScheduleScreen extends StatefulWidget {
  const SearchScheduleScreen({super.key});
  @override
  State<SearchScheduleScreen> createState() => _SearchScheduleScreenState();
}

class _SearchScheduleScreenState extends State<SearchScheduleScreen> {
  final TextEditingController _ctrl = TextEditingController();
  Timer? _debounce;
  bool _loading = false;
  List<Map<String,dynamic>> _results = [];
  Set<String> _favorites = {}; // entries like "group:<id>" or "teacher:<id>"

  @override
  void initState(){
    super.initState();
    _loadFavorites();
    _ctrl.addListener(() { setState((){}); });
  }

  Future<void> _loadFavorites() async {
    final p = await SharedPreferences.getInstance();
    final raw = p.getString('fav_plans');
    if (raw == null) return;
    try {
      final List<dynamic> l = jsonDecode(raw);
      setState(()=> _favorites = l.map((e) => e.toString()).toSet());
    } catch (_) {}
  }

  Future<void> _saveFavorites() async {
    final p = await SharedPreferences.getInstance();
    await p.setString('fav_plans', jsonEncode(_favorites.toList()));
  }

  void _onQueryChanged(String q){
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds:350), (){ _doSearch(q.trim()); });
  }

  Future<void> _doSearch(String q) async {
    if (q.isEmpty) {
      setState(()=> _results = []);
      return;
    }
    setState(()=> _loading = true);
    final List<Map<String,dynamic>> out = [];
    try {
      // groups – skorzystaj z ClassesRepository, żeby logika była wspólna
      final grows = await ClassesRepository.searchGroups(q, limit: 50);
      for (final m in grows) {
        out.add({
          'type':'group',
          'id': (m['id'] ?? '').toString(),
          'title': (m['kod_grupy'] ?? '').toString(),
          'subtitle': '',
        });
      }
    } catch (e) {
      debugPrint('[Search] groups err $e');
    }
    try {
      // teachers – również via ClassesRepository
      final trows = await ClassesRepository.searchTeachers(q, limit: 50);
      for (final m in trows) {
        out.add({
          'type':'teacher',
          'id': (m['id'] ?? '').toString(),
          'title': (m['nazwa'] ?? '').toString(),
          'subtitle': '',
        });
      }
    } catch (e) {
      debugPrint('[Search] teachers err $e');
    }

    // dedupe by type+id
    final seen = <String>{};
    final deduped = <Map<String,dynamic>>[];
    for (final r in out) {
      final key = '${r['type']}:${r['id']}';
      if (seen.contains(key)) continue; seen.add(key); deduped.add(r);
    }
    setState(()=> _results = deduped);
    setState(()=> _loading = false);
  }

  bool _isFav(Map<String,dynamic> item) => _favorites.contains('${item['type']}:${item['id']}');

  void _toggleFav(Map<String,dynamic> item) {
    final key = '${item['type']}:${item['id']}';
    setState((){
      if (_favorites.contains(key)) _favorites.remove(key); else _favorites.add(key);
    });
    _saveFavorites();
  }

  @override
  void dispose(){
    _debounce?.cancel();
    _ctrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context){
     return Scaffold(
       backgroundColor: Colors.white,
       appBar: AppBar(
         leading: IconButton(
           icon: const Icon(MyUz.chevron_left, size: 24),
           onPressed: () => Navigator.pop(context),
         ),
         titleSpacing: 0,
         // AppBar ma tło surface (MD3) — pole wyszukiwania jest 'transparentne' z samym hintem
         backgroundColor: Theme.of(context).colorScheme.surface,
         foregroundColor: Theme.of(context).colorScheme.onSurface,
         elevation: 0,
         title: SizedBox(
           height: 44,
           child: TextField(
             controller: _ctrl,
             autofocus: true,
             textInputAction: TextInputAction.search,
             decoration: InputDecoration(
               hintText: 'Szukaj planu grupy lub nauczyciela',
               border: InputBorder.none,
               isDense: true,
               contentPadding: const EdgeInsets.symmetric(vertical: 10, horizontal: 8),
             ),
             onChanged: _onQueryChanged,
           ),
         ),
         actions: [
           TextButton(
             onPressed: () => Navigator.pop(context),
             child: const Text('Anuluj'),
           ),
         ],
       ),
       body: SafeArea(
         child: Column(
           children: [
            // full-width divider pod AppBar, zgodnie z wytycznymi
            const Divider(height: 1, thickness: 1),
             Expanded(
               child: _loading
                 ? const Center(child: CircularProgressIndicator())
                 : (_results.isEmpty
                     ? const Center(child: Text('Brak wyników'))
                     : Material(
                         color: Colors.white,
                         child: ListView.separated(
                           itemCount: _results.length,
                           separatorBuilder: (_, __) => const Divider(height: 1, thickness: 1),
                           itemBuilder: (context, i) {
                             final item = _results[i];
                             final type = (item['type'] ?? 'group').toString();
                             final title = (item['title'] ?? '').toString();
                             final subtitle = (item['subtitle'] ?? '').toString();
                             final isFav = _isFav(item);
                             final IconData iconData = type == 'teacher' ? MyUz.user_01 : MyUz.users_01;
                             return ListTile(
                               contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                               // bez kolorowego tła pod ikoną; wyrównanie pionowe na środku
                               leading: SizedBox(
                                 width: 48,
                                 child: Align(
                                   alignment: Alignment.center,
                                   child: Icon(iconData, size: 20, color: const Color(0xFF1D192B)),
                                 ),
                               ),
                               title: Text(title, style: Theme.of(context).textTheme.bodyLarge),
                               subtitle: subtitle.isNotEmpty ? Text(subtitle, style: Theme.of(context).textTheme.bodySmall) : null,
                               trailing: IconButton(
                                 icon: Icon(isFav ? Icons.favorite : Icons.favorite_border, color: isFav ? Colors.redAccent : null),
                                 onPressed: () => _toggleFav(item),
                               ),
                               onTap: () async {
                                 if (item['type']=='group') {
                                   final String id = (item['id'] ?? '').toString();
                                   final String code = (item['title'] ?? '').toString();
                                   final navigator = Navigator.of(context);
                                   // Otwórz ekran z pełnym widokiem planu grupy (istniejący GroupScheduleScreen)
                                   final res = await navigator.push<Map<String,dynamic>?>(MaterialPageRoute(builder: (_) => GroupScheduleScreen(groupCode: code, groupId: id)));
                                    if (!mounted) return;
                                    if (res != null && res['apply'] == true) {
                                      if (id.isNotEmpty && code.isNotEmpty) {
                                        await ClassesRepository.setGroupPrefsById(groupId: id, groupCode: code, subgroups: []);
                                      } else {
                                        await ClassesRepository.setGroupPrefs(code, []);
                                      }
                                      if (!mounted) return;
                                      navigator.pop({'type':'group','code': code, 'id': id});
                                    }
                                    return;
                                 }
                                 if (item['type']=='teacher') {
                                   final navigator = Navigator.of(context);
                                   final String id = (item['id'] ?? '').toString();
                                   final String name = (item['title'] ?? '').toString();
                                   final res = await navigator.push<Map<String,dynamic>?>(MaterialPageRoute(builder: (_) => TeacherScheduleScreen(teacherId: id, teacherName: name)));
                                   if (!mounted) return;
                                   if (res != null && res['apply'] == true) {
                                     // Zapisz preferencje nauczyciela
                                     try {
                                       await ClassesRepository.setTeacherPrefsById(id);
                                     } catch (_) {}
                                     navigator.pop({'type':'teacher','id': id, 'name': name});
                                   }
                                   return;
                                 }
                                 Navigator.pop(context, null);
                               },
                             );
                           },
                         ),
                       )
                   ),
             ),
           ],
         ),
       ),
     );
   }
 }
