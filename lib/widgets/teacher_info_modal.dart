import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/theme/text_style.dart';

/// Modal z informacjami o nauczycielu.
/// Użycie: await TeacherInfoModal.show(context, teacherId, teacherName);
class TeacherInfoModal {
  static Future<void> show(BuildContext context, String teacherId, String fallbackName) async {
    return showDialog<void>(
      context: context,
      builder: (ctx) {
        return FutureBuilder<Map<String,dynamic>?>(
          future: ClassesRepository.getTeacherDetails(teacherId),
          builder: (context, snapshot) {
            final loading = snapshot.connectionState == ConnectionState.waiting;
            final details = snapshot.data;
            final cs = Theme.of(context).colorScheme;
            final name = details?['nazwa'] as String? ?? fallbackName;
            final email = details?['email'] as String?;
            final instRaw = details?['instytut'] as String? ?? details?['institute'] as String?;
            // Użyj centralnego helpera z ClassesRepository, aby uniknąć duplikacji logiki i dziwnego dzielenia
            final institutes = ClassesRepository.parseInstitutes(instRaw);

            return AlertDialog(
              title: Text(loading ? 'Ładowanie...' : name),
              content: SizedBox(
                width: double.maxFinite,
                child: loading
                    ? const SizedBox(height: 80, child: Center(child: CircularProgressIndicator()))
                    : Column(
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
                                    final messenger = ScaffoldMessenger.of(ctx);
                                    final navigator = Navigator.of(ctx);
                                    await Clipboard.setData(ClipboardData(text: email));
                                    navigator.pop();
                                    messenger.showSnackBar(const SnackBar(content: Text('Adres e-mail skopiowany')));
                                  },
                                  tooltip: 'Kopiuj e-mail',
                                )
                              ],
                            ),
                          ],
                          if (institutes.isNotEmpty) ...[
                            const SizedBox(height: 8),
                            // Pokaż instytuty skonsolidowane w jednym polu (maks. kilka linii) — użyj SelectableText aby można było kopować
                            SelectableText(
                              institutes.join(', '),
                              maxLines: 4,
                              style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant),
                            ),
                          ],
                          if ((email == null || email.isEmpty) && institutes.isEmpty)
                            const Text('Brak dodatkowych informacji'),
                        ],
                      ),
              ),
              actions: [
                TextButton(onPressed: () => Navigator.of(ctx).pop(), child: const Text('Zamknij')),
              ],
            );
          },
        );
      },
    );
  }
}
