# Spider

A Spider Solitaire game where you can actually *play the game*.

Revolutionary, we know.

---

## What is This?

Spider is a full-featured Spider Solitaire implementation built with libGDX that does something absolutely unheard of in 2026: it respects your time, your wallet, and your sanity.

**What You Get:**
- Complete Spider Solitaire with 1-6 suit difficulties
- Full undo system (with scoring penalty, we're not monsters)
- Daily challenge mode with seeded deals
- Practice mode with infinite replayability
- Auto-move with intelligent priority system
- Retry feature to beat your previous score on the same deal
- Cross-platform: Desktop, Android, iOS

**What You Don't Get:**
- Ads interrupting you every 47 seconds
- "Watch this ad to undo your last move"
- Energy systems that run out when you start having fun
- $99.99 "Best Value!" card pack purchases
- "Limited time offers" that have been running since 2019
- Fake X buttons that open the App Store
- Battle passes for a single-player card game (yes, other games do this)

Just solitaire. That you play. With your hands. On a screen.

We're basically cave people over here.

---

## The Modern Mobile Solitaire Experience We're NOT Delivering

```
╔════════════════════════════════════════════════════════════╗
║                                                            ║
║         [ WATCH AD TO UNDO YOUR LAST MOVE ]                ║
║                                                            ║
║                   0:29 remaining                           ║
║                                                            ║
║              [ X ]  <-- (does nothing for 5 seconds)       ║
║                                                            ║
║     Or purchase 50 Undo Gems for only $4.99!               ║
║                                                            ║
╚════════════════════════════════════════════════════════════╝
```

In Spider, you just press Undo. It costs you 10 points. That's it. No video. No purchase. No "daily undo limit reached."

---

## Game Rules

Spider Solitaire, if you've somehow avoided the 47 ad-infested versions on the app store:

### Setup
- **10 tableau columns** - 4 columns with 6 cards, 6 columns with 5 cards
- **Stock pile** - 50 remaining cards, dealt 10 at a time
- **Goal** - Remove all cards by building King-to-Ace sequences of the same suit

### How to Play
1. **Move cards** in descending rank order (King → Queen → Jack → ... → Ace)
2. **Build sequences** - cards of any suit can stack, but only same-suit sequences can be removed
3. **Complete a suit** - when you have K-Q-J-10-9-8-7-6-5-4-3-2-A of one suit in sequence, it's removed
4. **Deal from stock** - adds one card to each tableau (cannot deal if any column is empty)
5. **Win** - remove all required suits (8 suits for 1-4 difficulty, 10 for 5, 12 for 6)

### Controls
| Action | Input |
|--------|-------|
| Move cards | Drag and drop |
| Auto-move | Double-tap a card |
| Undo | `Ctrl+Z` or tap Undo button |
| Pause/Menu | `ESC` or tap Menu button |
| Collapse column | Tap the face-down cards |

---

## Difficulty Levels

We have 6 suits. Yes, six. Because four suits is for cowards.

| Difficulty | Suits | Cards | Required Completions | Description |
|------------|-------|-------|---------------------|-------------|
| 1 | ♠ | 104 | 8 suits | Easy - one suit, pure zen |
| 2 | ♠♥ | 104 | 8 suits | Classic - the way God intended |
| 3 | ♠♥♦ | 104 | 8 suits | Challenging - three's a crowd |
| 4 | ♠♥♦♣ | 104 | 8 suits | Hard - full traditional deck |
| 5 | ♠♥♦♣🐴 | 130 | 10 suits | Expert - yes, horses |
| 6 | ♠♥♦♣🐴⚽ | 156 | 12 suits | Impossible - horses AND balls |

The suits:
- **♠ Spades** - Black/Gray
- **♥ Hearts** - Red
- **♦ Diamonds** - Blue
- **♣ Clubs** - Green
- **🐴 Horses** - Orange (it's a horseshoe, work with us here)
- **⚽ Balls** - Purple

---

## Scoring

```
Starting Score:     500 points
Per Move:            -1 point
Per Undo:           -10 points
Per Completed Suit: +100 points
```

Your final score reflects how efficiently you solved the puzzle. Negative scores are possible. We don't judge. Much.

---

## Game Modes

### Solo Practice
New random deal every time. Perfect for:
- Learning the game
- Killing time
- Avoiding whatever you should actually be doing

### Daily Grind
Same deal for everyone, every day. Perfect for:
- Competing with friends
- Proving you're better at cards than your coworkers
- Having a consistent excuse to procrastinate

The daily seed is based on the date, so everyone gets the same puzzle. No server required. No accounts. No "connect to Facebook to see the leaderboard."

---

## Building From Source

Because we're not hiding anything. No server-side manipulation of deal RNG here.

```bash
# Run the game (desktop)
./gradlew lwjgl3:run

# Build a JAR file
./gradlew lwjgl3:jar

# Platform-specific builds (smaller file sizes)
./gradlew lwjgl3:jarWin    # Windows
./gradlew lwjgl3:jarMac    # macOS
./gradlew lwjgl3:jarLinux  # Linux

# Run tests
./gradlew test

# Clean everything
./gradlew clean
```

Output JAR lands in `lwjgl3/build/libs/`.

---

## Project Structure

```
spider/
├── core/               # Game logic (all platforms share this)
│   └── ape/spider/
│       ├── Main.java              # Application entry point
│       ├── GameScreen.java        # The actual game (1600+ lines of solitaire)
│       ├── GameConfig.java        # Mode and difficulty settings
│       ├── GameResult.java        # Score tracking and comparison
│       ├── MainMenuScreen.java    # Main menu
│       ├── DifficultySelectScreen.java
│       ├── ResultScreen.java      # Victory/defeat screen
│       ├── SplashScreen.java      # Logo display
│       └── InfoScreen.java        # About screen
├── lwjgl3/             # Desktop launcher
├── android/            # Android launcher
├── ios/                # iOS launcher (RoboVM)
└── assets/             # Game assets (not loot boxes)
```

---

## Technical Details

| Component | Technology |
|-----------|-----------|
| Framework | libGDX 1.13.1 |
| Physics | Box2D (not that cards need physics, but it's there) |
| Lighting | Box2DLights (for future ambient card glow, obviously) |
| Fonts | FreeType |
| Controllers | gdx-controllers (play solitaire with a gamepad, you absolute legend) |
| Java | 8+ compatible |
| Rendering | Custom ShapeRenderer cards (no sprite assets needed) |

### Mobile-First Design

This game follows modern mobile best practices:

- **Safe Area Handling** - Content respects notches, rounded corners, status bars, and home indicators. Your cards won't hide behind the notch on your iPhone or disappear under the navigation bar on Android.
- **Edge-to-Edge Display** - Full screen immersive experience on Android with proper cutout mode support.
- **Touch-Optimized** - Larger buttons on mobile devices for comfortable touch targets. No more accidentally hitting undo when you meant to hit menu.
- **Responsive Layout** - UI scales appropriately across phones, tablets, and desktop screens.
- **No Landscape Lock** - Play however you want to hold your device.

---

## Monetization Strategy

```
     ┌─────────────────────────────┐
     │                             │
     │      [ INTENTIONALLY ]      │
     │         [ BLANK ]           │
     │                             │
     └─────────────────────────────┘
```

---

## FAQ

**Q: Where are the in-app purchases?**
A: Between the "respect for players" and "basic human decency" sections of our design doc.

**Q: How do I get more undo tokens?**
A: There are no tokens. Press undo. Lose 10 points. That's it. That's the whole system.

**Q: I've been playing for an hour and haven't seen a single ad. Is this a bug?**
A: Feature.

**Q: Why six suits? That's not real Spider Solitaire.**
A: Coward.

**Q: How does this game make money?**
A: Bold of you to assume we've thought that far ahead.

**Q: Can I pay to reveal all cards?**
A: No. Suffer like the rest of us.

**Q: My kid accidentally spent $3,000 on SpiderBux.**
A: There is no SpiderBux. Check Candy Crush.

**Q: The Daily Grind seed - is it the same for everyone?**
A: Yes. It's just the date converted to a number. No servers, no accounts, no data harvesting. Revolutionary, we know.

**Q: Why are there horses and balls as suits?**
A: Because 4-suit Spider is too easy and we needed more suits. Traditional Spanish/Italian decks have coins and cups. We have horses and balls. Don't question it.

---

## A Brief History of Mobile Solitaire

```
2007: "Wow, I can play Solitaire on my phone!"
2010: "This free Solitaire app is pretty good!"
2013: "Hmm, there are some ads between games..."
2015: "I have to watch an ad to UNDO?"
2018: "Daily rewards require watching 5 ads minimum"
2020: "VIP subscription unlocks unlimited undos for $9.99/month"
2023: "Purchase the Golden Card Back Bundle - only $24.99!"
2025: "Solitaire Battle Pass Season 47 is here!"
2026: Spider releases. Confused players report "bug" of no monetization.
```

---

## A Message to Mobile Solitaire Publishers

You took a game that comes free with every Windows installation since 1990 and turned it into a monetization scheme. Solitaire. A single-player card game. You added:

- Energy systems
- Premium currencies
- VIP subscriptions
- Loot boxes (IN SOLITAIRE)
- Daily login rewards requiring ad views
- "Special event" card backs for $15

This is genuinely impressive in the most depressing way possible.

We're doing something different.

---

## System Requirements

- A device capable of running Java
- Fingers (optional but recommended)
- Absolutely no credit card information
- A tolerance for losing at 6-suit Spider (you will lose)
- A vague memory of when mobile games were just games

---

## Contributing

Found a bug? Want to add a feature? Open a PR.

We promise we won't ask you to watch a 30-second unskippable video before we review it.

We also won't require you to collect 500 "Contributor Tokens" to unlock the ability to submit issues.

---

## License

Free. Actually free. Not "free*" with an asterisk leading to 47 pages of terms explaining how we'll harvest your data and sell it to the highest bidder.

The source is right here. Build it yourself. We have nothing to hide.

---

## Final Words

Remember when Solitaire came with Windows and it was just... there? No account required? No internet connection? No "daily bonus" manipulation?

Remember when you could play a card game without being psychologically profiled for "engagement optimization"?

We remember.

---

<p align="center">
  <i>Spider: A solitaire game that's just a solitaire game.</i>
</p>

<p align="center">
  <code>Built with libGDX | Powered by spite for modern monetization | Made by NexiVIBE</code>
</p>

<p align="center">
  <b>No gems were harmed in the making of this game, because there are no gems.</b>
</p>

<p align="center">
  <sub>Now go play some cards. You've earned it by reading this far.</sub>
</p>
