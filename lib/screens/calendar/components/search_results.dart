import 'package:flutter/material.dart';
import 'package:my_uz/icons/my_uz_icons.dart';

typedef ItemTap = void Function(Map<String,dynamic> item);
typedef FavToggle = void Function(Map<String,dynamic> item);
typedef FavChecker = bool Function(Map<String,dynamic> item);

class SearchResults extends StatelessWidget {
  final List<Map<String,dynamic>> items;
  final ItemTap? onTapItem;
  final FavToggle? onToggleFavorite;
  final FavChecker? favoritesChecker;
  const SearchResults({super.key, required this.items, this.onTapItem, this.onToggleFavorite, this.favoritesChecker});

  @override
  Widget build(BuildContext context) {
    if (items.isEmpty) {
      return Center(child: Text('Brak wyników', style: Theme.of(context).textTheme.bodyMedium));
    }
    return ListView.separated(
      itemCount: items.length,
      separatorBuilder: (_, __) => const Divider(height: 1),
      itemBuilder: (context, i) {
        final it = items[i];
        final type = (it['type'] ?? 'group').toString();
        final title = (it['title'] ?? '').toString();
        final subtitle = (it['subtitle'] ?? '').toString();
        final isFav = favoritesChecker?.call(it) ?? false;
        final leading = type == 'teacher' ? Icon(MyUz.user_01, size: 20) : Icon(MyUz.users_01, size: 20);
        return ListTile(
          leading: leading,
          title: Text(title, style: Theme.of(context).textTheme.bodyLarge),
          subtitle: subtitle.isNotEmpty ? Text(subtitle, style: Theme.of(context).textTheme.bodySmall) : null,
          trailing: IconButton(
            icon: Icon(isFav ? Icons.favorite : Icons.favorite_border, color: isFav ? Colors.redAccent : null),
            onPressed: () { if (onToggleFavorite!=null) onToggleFavorite!(it); },
          ),
          onTap: () { if (onTapItem!=null) onTapItem!(it); },
        );
      },
    );
  }
}

