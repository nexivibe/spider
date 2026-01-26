# Spider

A game where you can actually *play the game*.

Revolutionary, we know.

---

## What is This?

Spider is a libGDX game that does something absolutely unheard of in 2026: it respects your time, your wallet, and your sanity.

**Features:**
- No ads interrupting you every 47 seconds
- No "watch this ad to continue playing the game you already downloaded"
- No "energy system" that runs out precisely when you start having fun
- No $99.99 "Best Value!" gem packs
- No "limited time offers" that have been running since 2019
- No fake X buttons that open the App Store
- No "rate us 5 stars" popup appearing before the title screen loads
- No "connect to Facebook to unlock basic features"
- No battle passes, season passes, VIP passes, or any other passes that require your credit card

Just a game. That you play. With your hands. On a screen.

We're basically cave people over here.

---

## The Modern Mobile Experience We're NOT Delivering

```
╔════════════════════════════════════════════════════════════╗
║                                                            ║
║         [ WATCH AD TO CONTINUE READING THIS README ]       ║
║                                                            ║
║                   advancement: 0:29 remaining               ║
║                                                            ║
║              [ X ]  <-- (does nothing for 5 seconds)       ║
║                                                            ║
╚════════════════════════════════════════════════════════════╝
```

Tired of games where you spend more time watching ads for other games than playing the actual game? Where the "free" game costs you $400 in microtransactions to be competitive? Where the entire gameplay loop is:

1. Tap
2. Wait
3. Pay or watch ad
4. Tap again
5. "You've run out of taps! Buy more taps for $4.99 or wait 4 hours"

Yeah, we don't do that here.

---

## Building From Source

Because we're not hiding anything. No server-side manipulation of drop rates here.

```bash
# Run the game (desktop)
./gradlew lwjgl3:run

# Build a JAR file
./gradlew lwjgl3:jar

# Platform-specific builds (for smaller file sizes)
./gradlew lwjgl3:jarWin    # Windows
./gradlew lwjgl3:jarMac    # macOS
./gradlew lwjgl3:jarLinux  # Linux

# Run tests (yes, we have those)
./gradlew test

# Clean everything
./gradlew clean
```

---

## Project Structure

```
spider/
├── core/           # The actual game (works on all platforms)
├── lwjgl3/         # Desktop launcher (LWJGL3)
├── android/        # Android (no predatory monetization included)
├── ios/            # iOS (same non-predatory experience)
└── assets/         # Game assets (not loot boxes)
```

---

## Technical Details

For those who care about such things:

| Thing | What It Is |
|-------|-----------|
| **Framework** | libGDX 1.13.1 |
| **Physics** | Box2D (follows laws of physics, not engagement metrics) |
| **Lighting** | Box2DLights |
| **Fonts** | FreeType (renders text that doesn't say "SALE! 90% OFF!") |
| **Controllers** | gdx-controllers (play with a gamepad like a civilized person) |
| **Java** | 8+ compatible |

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
A: We looked everywhere. Couldn't find them. Weird.

**Q: How do I get more gems/coins/crystals/diamonds/tokens?**
A: There aren't any. You just... play the game.

**Q: When does the battle pass season end?**
A: The concept of a "battle pass" ended when we decided not to implement one.

**Q: I've been playing for 5 minutes and haven't seen a single ad. Is this a bug?**
A: That's a feature. We know it's confusing in this day and age.

**Q: How does this game make money?**
A: Bold of you to assume we've thought that far ahead.

**Q: Can I pay to skip levels?**
A: No. You'll play them and you'll like it.

**Q: Where's the "VIP Subscription" that unlocks the full game?**
A: The download button unlocked the full game. That was it. That was the whole process.

**Q: My kid accidentally spent $3,000 on SpiderBux. How do I get a refund?**
A: There is no SpiderBux. Your kid is lying to you. Check Fortnite.

---

## A Brief History of Mobile Gaming

```
2007: "Wow, I can play games on my phone!"
2010: "Angry Birds is so fun and only costs $1!"
2013: "Hmm, this game is free but has some ads..."
2015: "I need to pay HOW MUCH to upgrade my town hall?"
2018: "Congratulations! You've unlocked a SENSE OF PRIDE AND ACCOMPLISHMENT"
2020: "Please watch this 30-second ad for a game worse than this one"
2023: "Limited time offer! Only $49.99 for 100 gems (was $50.00!)"
2025: "Subscribe for $19.99/month to remove ads from the menu screen"
2026: Spider releases, confused players report "bug" of no monetization
```

---

## A Message to Mobile Game Publishers

Your games aren't games. They're spreadsheets with particle effects. They're Skinner boxes wearing the skin of entertainment. They're engagement-optimized dopamine extraction machines designed by psychologists who should probably feel bad about themselves.

You've trained an entire generation to think that "free to play" means "pay to have fun" and that's genuinely impressive in the most depressing way possible.

The average mobile game has more dark patterns than a Gothic cathedral has... well, dark patterns. But the architectural kind. You know what we mean.

We're doing something different.

---

## System Requirements

- A device capable of running Java
- Fingers (optional but recommended)
- Absolutely no credit card information
- A vague memory of what games were like before 2012

---

## Contributing

Found a bug? Want to add a feature? Open a PR.

We promise we won't ask you to watch a 30-second unskippable video before we review it.

We also won't require you to collect 500 "Contributor Tokens" to unlock the ability to submit issues.

---

## License

Free. Actually free. Not "free*" with an asterisk leading to 47 pages of terms explaining how we'll harvest your data and sell it to the highest bidder.

---

## Final Words

Remember when games came on cartridges and the only microtransaction was buying another game?

Remember when "downloadable content" meant actual content and not a different colored hat?

Remember when mobile games were things like Snake and they were just... fun?

Remember when you could hand a kid a game and not worry they'd bankrupt you?

We remember.

---

<p align="center">
  <i>Spider: Because someone had to make a game that's just a game.</i>
</p>

<p align="center">
  <code>Built with libGDX | Powered by spite for modern monetization | Made with actual love for gaming</code>
</p>

<p align="center">
  <b>No gems were harmed in the making of this game, because there are no gems.</b>
</p>
