import 'package:flutter/material.dart';

class FullWidthDivider extends StatelessWidget {
  final double height;
  final Color? color;
  const FullWidthDivider({this.height = 1, this.color});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return Container(
      width: double.infinity,
      height: height,
      color: color ?? cs.outlineVariant,
    );
  }
}

