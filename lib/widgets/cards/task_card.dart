// Plik: lib/widgets/cards/task_card.dart
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:my_uz/icons/my_uz_icons.dart';
import 'package:my_uz/theme/text_style.dart';

/// TaskCard – karta “Zadania”
class TaskCard extends StatelessWidget {
  // --- POPRAWKA: ZMIENIONO WYSOKOŚĆ DLA SPÓJNOŚCI ---
  static const double _kHeightTask = 68; // Było 88
  // --- KONIEC POPRAWKI ---

  final String title;
  final DateTime deadline;
  final String? subject;
  final String? type;
  final Color? backgroundColor;
  final VoidCallback onTap;
  final bool showAvatar;

  const TaskCard({
    super.key,
    required this.title,
    required this.deadline,
    this.subject,
    this.type,
    this.backgroundColor,
    required this.onTap,
    this.showAvatar = false,
  });

  @override
  Widget build(BuildContext context) {
    final scale = MediaQuery.of(context).textScaleFactor;
    final adaptive = scale > 1.0;
    final subj = subject?.trim() ?? '';
    final typ = type?.trim() ?? '';
    final hasSubj = subj.isNotEmpty;

    // Formatowanie daty (np. "Pt, 20 gru")
    final dateStr = _formatDate(deadline);

    final inner = Container(
      padding: const EdgeInsets.all(12),
      decoration: ShapeDecoration(
        color: backgroundColor ?? const Color(0xFFE8DEF8),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.center, // Wyśrodkowanie w pionie
        children: [
          Text(
            title,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: AppTextStyle.myUZLabelLarge.copyWith(
              color: const Color(0xFF1D192B),
              fontWeight: FontWeight.w500,
            ),
          ),
          // --- POPRAWKA: Zmieniono z 8 na 4, aby wyrównać się z EventCard i zmieścić w 68px ---
          const SizedBox(height: 4),
          // --- KONIEC POPRAWKI ---

          // --- POPRAWKA: Zmieniono Column na Row, aby zmieścić się w 68px ---
          Row(
            children: [
              // Data (zawsze widoczna)
              Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Icon(MyUz.calendar_check_02, size: 14, color: Color(0xFF49454F)),
                  const SizedBox(width: 4),
                  Text(
                    dateStr,
                    style: AppTextStyle.myUZBodySmall.copyWith(color: const Color(0xFF49454F), fontSize: 12),
                    maxLines: 1,
                  ),
                ],
              ),

              // Przedmiot (jeśli jest)
              if (hasSubj) ...[
                const SizedBox(width: 12), // Odstęp
                Expanded(
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(MyUz.graduation_hat_02, size: 14, color: Color(0xFF49454F)),
                      const SizedBox(width: 4),
                      Expanded(
                        child: Text(
                          subj,
                          style: AppTextStyle.myUZBodySmall.copyWith(color: const Color(0xFF49454F), fontSize: 12),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ),
                ),
              ]
              // Typ (np. Egzamin) jest ignorowany, jeśli jest przedmiot, by się zmieścić
              else if (typ.isNotEmpty) ...[
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    typ,
                    style: AppTextStyle.myUZBodySmall.copyWith(color: const Color(0xFF49454F), fontSize: 12),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ]
            ],
          )
          // --- KONIEC POPRAWKI ---
        ],
      ),
    );

    if (adaptive) {
      return InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(8),
        child: inner,
      );
    }
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(8),
      child: ConstrainedBox(
        constraints: const BoxConstraints(minHeight: _kHeightTask), // Używa teraz 68px
        child: inner,
      ),
    );
  }

  String _formatDate(DateTime d) {
    // Używa formatu "Śr, 5 maj"
    return DateFormat('E, d MMM', 'pl_PL').format(d);
  }
}