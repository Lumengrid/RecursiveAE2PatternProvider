---
navigation:
    parent: rep_intro/recu_intro-index.md
    title: Recursive AE2 Pattern Provider
    icon: recursiveae2patternprovider:recursiveae2patternprovider
categories:
- recursive_patterns
item_ids:
- recursiveae2patternprovider:recursiveae2patternprovider
---

# Recursive AE2 Pattern Provider

## 💡 Example Scenario

### Traditional AE2 Setup
To autocraft Iron Pickaxes, you manually create:
1. Iron Pickaxe pattern
2. Stick pattern  
3. Iron Ingot smelting pattern (if using raw iron)
4. Wood plank pattern (if making sticks from logs)
5. Log chopping pattern (if using whole logs)
6. ... and so on, manually tracing every dependency

### With Recursive AE2 Pattern Provider
1. Create ONE recursive Iron Pickaxe pattern
2. Install in Pattern Provider
3. **All dependencies auto-generated automatically!**

The mod traces the entire crafting tree and creates patterns for every missing intermediate step, 
making complex automation setups effortless to configure.