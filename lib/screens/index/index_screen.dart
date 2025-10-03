import 'package:flutter/material.dart';
import 'package:my_uz/theme/app_colors.dart';
import 'components/index_tabs.dart';

class IndexScreen extends StatefulWidget {
  const IndexScreen({super.key});

  @override
  State<IndexScreen> createState() => _IndexScreenState();
}

class _IndexScreenState extends State<IndexScreen> {
  int _selectedTab = 0;

  @override
  Widget build(BuildContext context) {
    final tt = Theme.of(context).textTheme;
    return Scaffold(
      backgroundColor: Colors.white, // wymuszenie białego tła
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Indeks',
              style: tt.headlineSmall?.copyWith(
                color: AppColors.myUZBlack,
              ),
            ),
            const SizedBox(height: 16),
            IndexTabs(
              selectedIndex: _selectedTab,
              onTabChanged: (i) => setState(() => _selectedTab = i),
            ),
            const SizedBox(height: 24),
            if (_selectedTab == 0)
              const Text('Oceny - zawartość')
            else
              const Text('Nieobecności - zawartość'),
          ],
        ),
      ),
    );
  }
}
