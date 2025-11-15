import 'dart:async';

import 'package:flutter/material.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Onboarding step 3 — wybór grupy.
/// - Pole wyszukiwania ma nieprzezroczyste tło (filled + fillColor)
/// - Po wybraniu elementu z listy kontroler pola zostaje ustawiony na pełny display (kod/nazwa)
/// - Jeśli wynik ma id, zapisujemy preferencję za pomocą setGroupPrefsById, w przeciwnym razie setGroupPrefs
class OnboardingPage3 extends StatefulWidget {
  final VoidCallback? onComplete;

  const OnboardingPage3({super.key, this.onComplete});

  @override
  State<OnboardingPage3> createState() => _OnboardingPage3State();
}

class _OnboardingPage3State extends State<OnboardingPage3> {
  final TextEditingController _searchController = TextEditingController();
  final FocusNode _focusNode = FocusNode();

  Timer? _debounce;
  bool _loading = false;
  List<Map<String, dynamic>> _results = [];
  Map<String, dynamic>? _selected; // chosen entry from results (may contain id/kod_grupy)
  String? _errorMsg;

  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _searchController.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  void _onQueryChanged(String q) {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 300), () => _doSearch(q));
  }

  Future<void> _doSearch(String q) async {
    final trimmed = q.trim();
    if (trimmed.isEmpty) {
      setState(() {
        _results = [];
        _errorMsg = null;
      });
      return;
    }

    setState(() {
      _loading = true;
      _errorMsg = null;
    });

    try {
      final rows = await ClassesRepository.searchGroups(trimmed, limit: 50);
      setState(() {
        _results = rows;
      });
    } catch (e) {
      setState(() {
        _errorMsg = 'Błąd wyszukiwania';
        _results = [];
      });
    } finally {
      setState(() {
        _loading = false;
      });
    }
  }

  String _displayForRow(Map<String, dynamic> row) {
    // Try to prefer kod_grupy, fall back to nazwa, then id
    final kod = (row['kod_grupy'] as String?)?.trim();
    final nazwa = (row['nazwa'] as String?)?.trim();
    final id = (row['id'] as String?)?.trim();
    if (kod != null && kod.isNotEmpty) return kod + (nazwa != null && nazwa.isNotEmpty ? ' — $nazwa' : '');
    if (nazwa != null && nazwa.isNotEmpty) return nazwa;
    if (id != null && id.isNotEmpty) return id;
    return row.toString();
  }

  Future<void> _onRowTap(Map<String, dynamic> row) async {
    final kod = (row['kod_grupy'] as String?)?.trim();
    final id = (row['id'] as String?)?.trim();
    final nazwa = (row['nazwa'] as String?)?.trim();

    final display = (kod != null && kod.isNotEmpty) ? (kod + (nazwa != null && nazwa.isNotEmpty ? ' — $nazwa' : '')) : (nazwa ?? id ?? '');
    // set field text to full display so user sees chosen entry
    _searchController.text = display;
    // keep selection
    setState(() {
      _selected = row;
      _results = [];
      _errorMsg = null;
    });
    // unfocus to close keyboard
    _focusNode.unfocus();

    // persist as preferences (best-effort)
    try {
      if (id != null && id.isNotEmpty && kod != null && kod.isNotEmpty) {
        // both id and code available -> save by id (preferred)
        await ClassesRepository.setGroupPrefsById(groupId: id, groupCode: kod, subgroups: const []);
      } else if (kod != null && kod.isNotEmpty) {
        await ClassesRepository.setGroupPrefs(kod, const []);
      } else if (id != null && id.isNotEmpty) {
        // if only id available, try to resolve metadata for code first
        final meta = await ClassesRepository.getGroupById(id);
        final resolvedCode = meta != null ? (meta['kod_grupy'] as String?) : null;
        if (resolvedCode != null && resolvedCode.isNotEmpty) {
          await ClassesRepository.setGroupPrefsById(groupId: id, groupCode: resolvedCode, subgroups: const []);
        } else {
          // fallback: save groupId into prefs directly
          final p = await SharedPreferences.getInstance();
          await p.setString('onb_group_id', id);
        }
      }
    } catch (e) {
      // ignore save error (best-effort)
    }
  }

  Widget _buildSearchField(BuildContext context) {
    return TextField(
      controller: _searchController,
      focusNode: _focusNode,
      onChanged: _onQueryChanged,
      textInputAction: TextInputAction.search,
      decoration: InputDecoration(
        hintText: 'Wpisz kod grupy lub jej fragment (np. "10A")',
        prefixIcon: const Icon(Icons.search),
        filled: true,
        // non-transparent background
        fillColor: Theme.of(context).colorScheme.surface,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide.none,
        ),
        contentPadding: const EdgeInsets.symmetric(vertical: 14, horizontal: 12),
        suffixIcon: _searchController.text.isNotEmpty
            ? IconButton(
          icon: const Icon(Icons.clear),
          onPressed: () {
            _searchController.clear();
            _onQueryChanged('');
            setState(() {
              _results = [];
              _selected = null;
            });
          },
        )
            : null,
      ),
    );
  }

  Widget _buildResults() {
    if (_loading) {
      return const Padding(
        padding: EdgeInsets.symmetric(vertical: 16),
        child: Center(child: CircularProgressIndicator()),
      );
    }
    if (_errorMsg != null) {
      return Padding(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: Center(child: Text(_errorMsg!, style: const TextStyle(color: Colors.red))),
      );
    }
    if (_results.isEmpty) {
      return const SizedBox.shrink();
    }
    return ListView.separated(
      shrinkWrap: true,
      physics: const ClampingScrollPhysics(),
      itemCount: _results.length,
      separatorBuilder: (_, __) => const Divider(height: 1),
      itemBuilder: (context, idx) {
        final row = _results[idx];
        final display = _displayForRow(row);
        return ListTile(
          title: Text(display),
          subtitle: row['id'] != null ? Text('ID: ${row['id']}') : null,
          onTap: () => _onRowTap(row),
        );
      },
    );
  }

  Future<void> _onContinue() async {
    // If user typed custom text without selecting, try to save as code
    if (_selected == null) {
      final typed = _searchController.text.trim();
      if (typed.isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Wybierz grupę lub wpisz jej kod.')));
        return;
      }
      // save as code (best-effort)
      try {
        await ClassesRepository.setGroupPrefs(typed, const []);
      } catch (_) {}
    }
    widget.onComplete?.call();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Onboarding — wybierz grupę'),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(16, 18, 16, 16),
          child: Column(
            children: [
              const Text(
                'Wybierz swoją grupę, aby zobaczyć plan i powiadomienia.',
                style: TextStyle(fontSize: 16),
              ),
              const SizedBox(height: 12),
              _buildSearchField(context),
              const SizedBox(height: 8),
              Expanded(
                child: SingleChildScrollView(
                  child: Column(
                    children: [
                      _buildResults(),
                      if (_selected != null) ...[
                        const SizedBox(height: 12),
                        Card(
                          elevation: 0,
                          child: ListTile(
                            leading: const Icon(Icons.check_circle, color: Colors.green),
                            title: Text(_displayForRow(_selected!)),
                            subtitle: _selected!['id'] != null ? Text('Zapisano preferencje') : null,
                            trailing: TextButton(
                              child: const Text('Zmień'),
                              onPressed: () {
                                setState(() {
                                  _selected = null;
                                });
                                _searchController.clear();
                                _focusNode.requestFocus();
                              },
                            ),
                          ),
                        ),
                      ],
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  Expanded(
                    child: ElevatedButton(
                      onPressed: _onContinue,
                      child: const Padding(
                        padding: EdgeInsets.symmetric(vertical: 12),
                        child: Text('Kontynuuj'),
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}