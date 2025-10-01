import 'package:flutter/material.dart';

class ConfirmModal {
  static Future<bool?> show(BuildContext context, {
    required String title,
    required String confirmText,
    required String cancelText,
  }) {
    return showDialog<bool>(
      context: context,
      barrierDismissible: false,
      builder: (context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
          title: Text(title, style: Theme.of(context).textTheme.titleMedium),
          actionsAlignment: MainAxisAlignment.spaceBetween,
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(false),
              child: Text(cancelText, style: const TextStyle(fontWeight: FontWeight.w500)),
            ),
            TextButton(
              onPressed: () => Navigator.of(context).pop(true),
              child: Text(confirmText, style: const TextStyle(fontWeight: FontWeight.w500)),
            ),
          ],
        );
      },
    );
  }
}
