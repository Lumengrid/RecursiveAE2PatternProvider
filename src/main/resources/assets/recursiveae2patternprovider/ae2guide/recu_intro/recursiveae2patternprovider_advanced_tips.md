---
navigation:
    parent: recu_intro/recu_intro-index.md
    icon:
    title: Advanced Tips
categories:
- recursive_patterns
---

# Recursive AE2 Pattern Provider

## 🔧 Advanced Tips

### Pattern Provider Setup
- Place recursive patterns in Pattern Providers connected to Molecular Assemblers
- The mod generates dependency patterns with the same substitute settings as the parent pattern
- Use multiple Pattern Providers for better parallel processing

### Recursion Depth Control
- **Depth 1**: Only direct ingredients
- **Depth 3**: Ingredients + their ingredients + their ingredients (recommended)
- **Unlimited (-1)**: Full dependency tree (use carefully)
- **Disabled (0)**: Normal AE2 behavior

### Performance Considerations
- Higher recursion depths generate more patterns but provide complete automation
- Monitor your Pattern Provider capacity when using deep recursion
- Consider using multiple Pattern Providers for complex recipes 