import 'package:flutter/material.dart';
import 'components/index_tabs.dart';
import 'package:my_uz/theme/text_style.dart';
import 'package:my_uz/theme/app_colors.dart';

class IndexScreen extends StatefulWidget {
  const IndexScreen({super.key});

  @override
  State<IndexScreen> createState() => _IndexScreenState();
}

class _IndexScreenState extends State<IndexScreen> {
  int _selectedTab = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white, // cały ekran na białym tle
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Nagłówek "Indeks" wyrównany do lewej
              Align(
                alignment: Alignment.centerLeft,
                child: Text(
                  'Indeks',
                  style: AppTextStyle.myUZHeadlineSmall.copyWith(color: AppColors.myUZBlack),
                ),
              ),
              const SizedBox(height: 12),
              // Tylko TabBar w stylu z makiety (328px max szerokości – zrobione w IndexTabs)
              IndexTabs(
                selectedIndex: _selectedTab,
                onTabChanged: (i) => setState(() => _selectedTab = i),
              ),
              const SizedBox(height: 24),
              Expanded(
                child: Align(
                  alignment: Alignment.topLeft,
                  child: _selectedTab == 0
                      ? const Text('Oceny - zawartość')
                      : const Text('Nieobecności - zawartość'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
