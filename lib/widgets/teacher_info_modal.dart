import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:my_uz/services/classes_repository.dart';
import 'package:my_uz/theme/text_style.dart';

/// Modal z informacjami o nauczycielu.
/// Użycie: await TeacherInfoModal.show(context, teacherId, teacherName);
class TeacherInfoModal {
  static Future<void> show(BuildContext context, String teacherId, String fallbackName) async {
    return showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
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
            final institutes = ClassesRepository.parseInstitutes(instRaw);

            return SafeArea(
              child: Container(
                margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                padding: const EdgeInsets.fromLTRB(16, 16, 16, 12),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(child: Text(loading ? 'Ładowanie...' : name, style: AppTextStyle.myUZTitleLarge.copyWith(fontWeight: FontWeight.w600))),
                        IconButton(icon: const Icon(Icons.close), onPressed: () => Navigator.of(ctx).pop()),
                      ],
                    ),
                    const SizedBox(height: 8),
                    if (loading)
                      const SizedBox(height: 80, child: Center(child: CircularProgressIndicator()))
                    else ...[
                      if (email != null && email.isNotEmpty) ...[
                        const Text('E-mail:'),
                        Row(
                          children: [
                            Expanded(child: SelectableText(email)),
                            IconButton(
                              icon: const Icon(Icons.copy, size: 20),
                              onPressed: () async {
                                final messenger = ScaffoldMessenger.of(ctx);
                                await Clipboard.setData(ClipboardData(text: email));
                                Navigator.of(ctx).pop();
                                messenger.showSnackBar(const SnackBar(content: Text('Adres e-mail skopiowany')));
                              },
                              tooltip: 'Kopiuj e-mail',
                            )
                          ],
                        ),
                      ],
                      if (institutes.isNotEmpty) ...[
                        const SizedBox(height: 8),
                        SelectableText(
                          institutes.join(', '),
                          maxLines: 4,
                          style: AppTextStyle.myUZBodySmall.copyWith(color: cs.onSurfaceVariant),
                        ),
                      ],
                      if ((email == null || email.isEmpty) && institutes.isEmpty)
                        const Text('Brak dodatkowych informacji'),
                    ],
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }
}
