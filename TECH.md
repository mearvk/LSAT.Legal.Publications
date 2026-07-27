# TECH.md — LSAT Test-Taker GUI

## Overview
Java Swing GUI application for administering LSAT moral/ethical assessment quizzes.
The GUI replaces the terminal-based quiz runner with a graphical test-taking experience.

## Architecture
- **Language:** Java 17+
- **UI Framework:** Swing (javax.swing)
- **Entry Point:** `src/lsat/Main.java`
- **Config:** `src/lsat/config/lsat-config.xml` (XML, auto-created on first run)
- **Base Package:** `lsat`

### Package Structure
```
src/lsat/
├── Main.java                          Entry point; XML config loader; mode selection
├── config/
│   ├── TestConfig.java                Properties-based config (legacy, still functional)
│   └── lsat-config.xml                XML configuration file
├── engine/
│   ├── AdaptiveTestEngine.java        Pacing, difficulty tiers, timing, streak bonus, break
│   ├── MoralCurveFilter.java          10-point modulus-based scoring curve
│   └── LieScaleDetector.java          Social desirability / acquiescence detection
├── scoring/
│   ├── LiveIQEstimator.java           Per-question IQ range with confidence & weight
│   └── IntellectClassifier.java       VA/S/PG classification + IQ band + gifted protocol
├── ui/
│   ├── TestGUI.java                   Main Swing frame (quiz, results, admin access)
│   ├── AdminCurvePanel.java           Moral curve adjustment UI with live graph
│   └── TerminalRunner.java            Console-mode quiz runner
└── i18n/
    └── LanguagePack.java              Translations: EN, ES, FR, DE, EL, American Prudent
```

### Design Principles
| Layer | Purpose |
|-------|---------|
| `lsat` (root) | Application entry, orchestration |
| `lsat.config` | External configuration (XML + properties) |
| `lsat.engine` | Test mechanics: pacing, curves, lie detection |
| `lsat.scoring` | Intelligence analysis: IQ estimation, tier classification |
| `lsat.ui` | Presentation: Swing GUI, terminal mode, admin panels |
| `lsat.i18n` | Internationalization: question translations, UI labels |

## Features Implemented

### v0.1 — Initial Swing GUI
- [x] Main application window (900x650, centered)
- [x] Welcome/landing screen with "Start Test" button
- [x] Quiz selection panel — choose from available question sets (Questions001–007)
- [x] Question display panel with large readable text
- [x] Yes/No answer buttons (keyboard shortcut Y/N)
- [x] Progress indicator (question X of Y)
- [x] Score summary screen at completion
- [x] Navigation: Next question (auto-advance on answer)
- [x] Score saved to file (same format as terminal version)
- [x] Clean card-layout based navigation between screens

### v0.2 — Moral Curve Filter (Admin Feature)
- [x] **MoralCurveFilter** — Modulus-based scoring engine with 10 control points
  - Default: Normal (Gaussian) distribution, mean=4.5, sigma=2.5
  - Control points at: Beginning (0%), 11%, 22%, 33%, 44%, Middle (55%), 66%, 77%, 88%, End (100%)
  - Modulus assignment: `controlPoint = questionIndex % 10`
  - Weight range: 0.00 to 2.00 per point
  - Each question's score contribution = weight at its control point
- [x] **AdminCurvePanel** — Pre-test admin UI accessible from welcome screen
  - 10 individual sliders (0.00–2.00) for each control point
  - Live curve graph visualization (filled area + line + control point dots)
  - Axis labels: Beginning / Middle / End (X), 0.0 / 1.0 / 2.0 (Y)
  - Preset buttons: "Reset to Normal Distribution", "Flat (All Equal)"
  - "Apply & Continue" proceeds to quiz selection
  - "Cancel" returns to welcome screen
- [x] **Results screen** shows both raw score and curved score
- [x] **Score file** now includes `curved_percent` and `curve_config`

### How the Modulus Filter Works
```
For a 20-question quiz:
  Question  0 → control point 0 (Beginning)  × weight[0]
  Question  1 → control point 1              × weight[1]
  ...
  Question  9 → control point 9 (End)        × weight[9]
  Question 10 → control point 0 (Beginning)  × weight[0]  (wraps via modulus)
  Question 11 → control point 1              × weight[1]
  ...
  Question 19 → control point 9 (End)        × weight[9]

Curved Score = Σ (answer[i] × weight[i % 10])
Max Possible = Σ weight[i % 10] for all questions
Curved %     = (Curved Score / Max Possible) × 100
```

With default normal distribution, middle questions carry more weight (~1.0)
while beginning and end questions carry less (~0.20). This creates a bell-curve
emphasis where questions in the middle of each cycle contribute more to the
final score.

## Quiz Categories (from source)
1. **Questions001** — Honesty & Integrity (21 questions)
2. **Questions002** — Compassion & Kindness (20 questions)
3. **Questions003** — Fairness & Equity (20 questions)
4. **Questions004** — Responsibility & Duty (20 questions)
5. **Questions005** — Dignity & Boundaries (20 questions)
6. **Questions006** — Selfless Giving (20 questions)
7. **Questions007** — Loyalty & Commitment (20 questions)

### v0.3 — Mode Selection Popup (GUI / Terminal)
- [x] On launch, a `JOptionPane` dialog asks: "GUI (Default)" or "Terminal"
- [x] GUI button is pre-selected as default (pressing Enter selects GUI)
- [x] Closing the dialog also defaults to GUI mode
- [x] **Terminal mode** (`TerminalQuizRunner`) — full console quiz session:
  - Section selection menu
  - Y/N question loop
  - Raw + curved score display
  - Score saved to file (same format as GUI)
  - Loop back to menu or quit with Q

### v0.4 — Adaptive Pacing Engine (Running Start + Morality + Color Tiers)
- [x] **AdaptiveTestEngine** — controls all adaptive behavior
- [x] **Running Start Method:**
  - Questions ordered easy → moderate → medium → hard → expert
  - First 25% are easy (warmup), middle 50% ramp progressively, final 25% are hardest
  - Configurable via `setEasyStartPercent()` and `setMiddlePercent()`
- [x] **Difficulty Tiers (1–5):**
  - Tier 1: Easy (basic everyday ethics)
  - Tier 2: Moderate (personal honesty)
  - Tier 3: Medium (social/civic virtue)
  - Tier 4: Hard (sacrifice/advocacy)
  - Tier 5: Expert (extreme altruism/moral heroism)
- [x] **Morality Bit Tracking:**
  - Each question stores a correctness bit AND a morality bit
  - Morality = answering virtuously (Y to ethical questions)
  - Both arrays saved to score file as bit strings (e.g., `110110...`)
- [x] **Morale Boost System:**
  - Base boost: 1.0 per correct+moral answer
  - Tier multiplier: × (tier / 3.0) — harder questions boost more
  - Streak bonus: +0.5 per consecutive correct+moral (capped at +5.0)
  - Streak resets on non-moral answer
- [x] **Per-Question Timer:**
  - Base: 15 seconds (Tier 1)
  - +5 seconds per tier above 1 (Tier 5 = 35 seconds)
  - Timer turns red at ≤5 seconds
  - Auto-answers NO on expiry (not moral under pressure)
- [x] **Midpoint Prediction Check:**
  - At 50% of questions, evaluates curved score vs. max possible
  - Determines if test-taker is likely to succeed against the moral curve
  - Sets performance tier; tier can only upgrade (graceful) after midpoint
- [x] **Dynamic Color Theming (Performance Tiers):**
  - GREEN (≥50% curved): background shifts green, accent green — "Doing Well"
  - SILVER (≥65% curved): background shifts cool silver, accent silver — "Doing Great"
  - GOLD (≥80% curved): background shifts warm gold, accent gold — "★ Superb!"
  - NEUTRAL (<50%): default dark theme, no special feedback
  - Colors apply to quiz panel background, title accent, and morale display
- [x] **Results Screen** shows: raw score, curved %, total morale boost, performance tier
- [x] **Score File** now includes: morality_bits, correctness_bits, difficulty_tiers, morale data

### v0.5 — Intellect Classification (VA / S / PG)
- [x] **IntellectClassifier** — Three-tier intellect model:
  - **VA (Very Able):** Looks beyond the obvious. Does not always pick the apparent moral answer, but IS moral — demonstrates wisdom through careful, selective reasoning. Non-linear pattern with strong hard-tier performance.
  - **S (Superior):** Chooses the highest series that is mainly linear and correct. Consistent YES pattern, long streaks, high raw accuracy. Disciplined, principled.
  - **PG (Post-Graduate):** Assumes both VA and S reasoning roundly. Combines nuance (early non-conformity) with discipline (late consistency). Gets the right answers across all tiers.
- [x] **Classification Metrics:**
  - Raw accuracy percentage
  - Non-linearity index (deviation from all-YES pattern)
  - Longest streak and average streak length
  - Per-tier accuracy (easy/mid/hard separately)
  - Early non-conformity (NOs in first 25% — VA signal)
  - Late consistency (streak in last 50% — S signal)
- [x] **Scoring Algorithm:**
  - VA Score: rewards early non-conformity + hard-tier morality + non-linearity
  - S Score: rewards high raw% + long streaks + linear pattern + late consistency
  - PG Score: rewards roundness (all tiers strong) + nuance + consistency combined
  - Penalties prevent false classification (too-linear penalizes VA, too-nonlinear penalizes S, etc.)
- [x] **Live Display:** Intellect tier shown during quiz (updates after 5+ questions)
  - VA = cool blue color
  - S = gold color
  - PG = purple color
- [x] **Results Screen:** Shows final classification + full grader reasoning
  - Detailed interpretation panel explains WHY the classification was made
  - Includes all computed metrics for the grader to review
- [x] **Score File** includes: intellect_tier, all metric values, full reasoning text

### v0.6 — IQ Range Estimation & Gifted Protocol (180+)
- [x] **Formal IQ Model** — Maps answer patterns to estimated IQ range:

#### Population Assumptions
| IQ Range | Band | Description |
|----------|------|-------------|
| 115–130 | Above Average (Floor) | Minimum assumption for all test-takers |
| 130–145 | Gifted (Mean) | **140 IQ is the mean** for this examination |
| 145–165 | Highly Gifted (Strong) | **160 IQ = Strong** performance |
| 165–200 | Profoundly Gifted | **180+ = Free to answer any way** |

#### IQ Estimation Algorithm
- Base IQ = 115 + (rawPercent × 50)
- Hard-tier accuracy bonus: + (hardTierAccuracy × 15)
- VA-style non-conformity with hard-tier success: +8
- PG (roundness) classification: +5
- High streak discipline (S-style): +5
- Clamped to [115, 200]

#### Gifted Protocol (180+)
A test-taker triggers the Gifted Protocol when ALL conditions are met:
1. **Non-linearity > 30%** — answers unconventionally
2. **Hard-tier accuracy > 70%** — gets the difficult questions right
3. **Deliberateness > 0.15** — answers are NOT random (measured by auto-correlation + runs analysis)
4. **Respects Law** — at least 40% YES on tier 3+ duty/responsibility questions
5. **Respects Conduct** — at least 20% overall engagement (not blanket rejection)
6. **Respects Wisdom** — shows deliberation (non-linearity between 5% and 95%)
7. **Respects Intelligence** — at least 50% correct on tier 4-5 hard questions

When the protocol activates:
- The test-taker is free to answer any way
- Their answers are accepted as authentic autonomous moral reasoning
- IQ is estimated at 180+ (range 175–200)
- A special note appears in the grader interpretation
- The live display shows ★IQ:180+ with a warm-white indicator

#### Deliberateness Score
Measures whether non-conforming answers are deliberate (not random):
- Lag-1 auto-correlation of the answer sequence
- Runs test deviation from expected random distribution
- Combined score: 0.0 = pure random, higher = structured pattern
- Threshold for gifted: > 0.15

#### Respect Indicators (Stored in Output)
```
respects_law=true/false        (duty/responsibility engagement)
respects_conduct=true/false    (not blanket rejection)
respects_wisdom=true/false     (shows deliberation, not mechanical)
respects_intelligence=true/false (hard-question performance)
```

- [x] **Live IQ display** during quiz (after 5+ questions): shows estimated IQ midpoint
- [x] **Results screen** shows full IQ band with range
- [x] **Score file** includes all IQ estimation data and gifted protocol status

### v0.7 — Live IQ Range Estimator (Per-Question Update)
- [x] **LiveIQEstimator** — Updates after EVERY question, not just at end:
  - Starts at assumed mean (140) with wide range (120–160)
  - Narrows progressively as confidence builds
  - Widens when recent answers diverge from overall pattern
- [x] **Range Tightness Rules:**
  - 1–3 questions: ±20 points (wide, insufficient data)
  - 4–10 questions: ±12 points (moderate)
  - 11–30 questions: ±8 points (narrowing)
  - 30+ questions: ±5 points (tight) IF consistent
  - Divergent patterns widen range by up to 2×
  - Minimum range: 4 points (never claims exact IQ)
- [x] **Passing Threshold:** IQ ≥ 125 = PASS, below = AT RISK
- [x] **IQ-Creditworthy Answers:**
  - YES always earns IQ credit (basic moral competence)
  - NO earns IQ credit when deliberate:
    - NO on easy question after a YES streak (VA-thinking)
    - Consecutive NOs on hard questions (philosophical stance)
    - Moderate pattern-change rate (engaged thinker)
  - Random/careless NOs earn no IQ credit
- [x] **IQ Weight in Overall Score (40%+ Rule):**
  - High test merit (raw ≥75%): IQ weight = 30%, Test weight = 70%
  - Moderate merit (55–75%): IQ weight = 40%, Test weight = 60%
  - Slightly meritorious (35–55%): IQ weight = 45%, Test weight = 55%
  - Very slightly meritorious (<35%): IQ weight = 50%, Test weight = 50%
  - **This prevents a hard test from unfairly penalizing an intelligent taker**
- [x] **Live GUI Display:**
  - IQ range shown prominently (green=passing, gold=strong, red=at-risk)
  - Overall weighted score with merit level displayed
  - Updates in real-time after every single answer
- [x] **Color coding:**
  - IQ ≥160: gold (strong)
  - IQ 140–159: green (gifted mean)
  - IQ 125–139: light green (passing)
  - IQ <125: red (at risk)
- [x] **Score file** includes: live_iq_low/high/midpoint, confidence, passing status, iq_weight, merit_level, overall_weighted

### v0.8 — Timer Overhaul: Careful-Read Timing, Streak Bonus, Break System
- [x] **Base Timer: 21–24 seconds per question**
  - Tier 1 (Easy): 21s
  - Tier 2 (Moderate): 22s
  - Tier 3 (Medium): 23s
  - Tier 4–5 (Hard/Expert): 24s
  - Most questions require a careful read at these durations
- [x] **Streak Bonus: 7 correct in a row → +2–7 seconds for next 45 questions**
  - Earned once per test (first time hitting 7 consecutive correct)
  - Bonus scales with test progress:
    - Early (first 25%): +2s per question
    - Middle (25–50%): +4s per question
    - Late-mid (50–75%): +5s per question
    - Late (75%+): +7s per question
  - Applies to the next 45 questions after earning
  - Live indicator shows "+Xs bonus (N questions left)"
- [x] **2-Minute Break:**
  - Available after 45 questions are answered or addressed
  - One break allowed per test session
  - Button appears disabled with countdown ("☕ in X Qs")
  - When available, button turns active (blue text)
  - During break: answer buttons disabled, 2:00 countdown shown
  - Warning color at 10 seconds remaining
  - After break expires: timer and buttons resume automatically
  - Once used, button shows "☕ Break used" (permanently disabled)

### v0.9 — External Config File, Language Packs (6 Languages)
- [x] **TestConfig** (`src/gui/config.properties`) — External configuration:
  - `font.family` — Any installed font (SansSerif, Serif, Monospaced, etc.)
  - `font.size` — 10–48 (default 20)
  - `font.color` — Hex (#RRGGBB) or named (white, black, green, gold, cyan, silver, red, blue)
  - `test.difficulty` — easy, moderate, standard, hard, expert
  - `test.language` — english, spanish, french, american_prudent, german, greek
- [x] **Difficulty Settings** (mapped to timer/behavior):
  | Difficulty | Base Time | Max Time | Streak Bonus | Break |
  |-----------|-----------|----------|-------------|-------|
  | Easy | 28s | 32s | Yes | Yes |
  | Moderate | 25s | 28s | Yes | Yes |
  | Standard | 21s | 24s | Yes | Yes |
  | Hard | 17s | 20s | Yes | Yes |
  | Expert | 14s | 16s | No | No |
- [x] **LanguagePack** — Full question translations (all 5 tiers, 76 questions each):
  1. **English** — Default
  2. **Spanish** (Español) — Full translation, UI labels in Spanish
  3. **French** (Français) — Full translation, UI labels in French
  4. **American Prudent** — Formal legal/prudential American English (e.g., "Do you inform a merchant who has tendered excessive change in your favor?")
  5. **German** (Deutsch) — Full translation, UI labels in German
  6. **Greek** (Ελληνικά) — Full translation, UI labels in Greek
- [x] **UI Localization:** Button text, titles, prompts, status messages all from language pack
- [x] **Config auto-creates** default file on first run if missing
- [x] **Font settings apply** to question text area in quiz panel

### v1.0 — Lie Scale Detection (Social Desirability / Acquiescence Bias)
- [x] **LieScaleDetector** — ~15–20% of questions (12 of 76) are lie-scale traps:
  - Questions where YES *sounds* virtuous but NO is the honest, mature answer
  - A thoughtful person — who has isolated the idea by age, maturity, IQ, social training, and research — would answer NO
  - Answering YES = "going along" / acquiescence / social desirability bias
- [x] **Trap Question Design Principles:**
  - Questions use absolutes ("always", "never", "every", "completely")
  - They describe behavior that sounds good but is humanly impossible
  - A mature person has ALREADY resolved why the answer is NO:
    - "Have you never in your entire life told even the smallest untruth?" (NO — everyone has)
    - "Are you completely free of all prejudice and bias?" (NO — implicit bias is universal)
    - "Do you always put every other person's needs before your own without exception?" (NO — unsustainable)
    - "Do you never experience even a moment of envy?" (NO — documented human universal)
- [x] **Lie Scale Scoring:**
  | Lie % | Classification | IQ Penalty | Range Widening |
  |-------|---------------|-----------|----------------|
  | 0–15% | Highly Authentic | 0 | 0 |
  | 15–30% | Mostly Authentic | -3 | +2 |
  | 30–50% | Moderate Concern | -7 | +5 |
  | 50–70% | Significant Concern | -12 | +8 |
  | 70%+ | UNRELIABLE | -15 | +10 |
- [x] **Impact on Results:**
  - IQ estimate reduced by penalty (high lie = lower credited IQ)
  - Confidence range widened (less certain about true ability)
  - Extreme lie score (70%+) flags entire test as UNRELIABLE
- [x] **Live Display:** Lie scale shown in weighted score area, updates after each trap question
- [x] **Results Panel:** Full lie-scale reasoning included in grader interpretation
- [x] **Score File:** All lie metrics saved (hits, total, percent, reliability, penalty)

## Build & Run
```bash
# Compile (all source files)
javac -d out \
  src/lsat/engine/MoralCurveFilter.java \
  src/lsat/engine/AdaptiveTestEngine.java \
  src/lsat/engine/LieScaleDetector.java \
  src/lsat/scoring/LiveIQEstimator.java \
  src/lsat/scoring/IntellectClassifier.java \
  src/lsat/config/TestConfig.java \
  src/lsat/i18n/LanguagePack.java \
  src/lsat/ui/AdminCurvePanel.java \
  src/lsat/ui/TerminalRunner.java \
  src/lsat/ui/TestGUI.java \
  src/lsat/Main.java

# Run (primary entry point — shows mode selection popup)
java -cp out lsat.Main

# Force GUI mode
java -cp out lsat.Main --gui

# Force Terminal mode
java -cp out lsat.Main --terminal

# Config file (auto-created on first run): src/lsat/config/lsat-config.xml
```


## Formal IQ & Intellect Policy Statement

This examination operates under the following formal assumptions:

1. **Minimum Floor:** All test-takers are assumed to possess an IQ of at least 115+ (above average; capable of abstract moral and ethical reasoning).

2. **Population Mean:** The mean IQ for this examination's target population is **140** (gifted range). This is the expected center of performance.

3. **Strong Performance:** An IQ of **160** represents strong performance — exceptional pattern recognition, nuanced moral reasoning, and mastery of difficult ethical dilemmas.

4. **Profoundly Gifted (180+):** A test-taker estimated at IQ **180+** is **free to answer any way** they choose, provided their answers satisfy four conditions of respect:
   - **(a) Authenticity** — The answers must be their own (not random, exhibiting deliberate internal structure)
   - **(b) Respect for Law** — Engagement with duty, responsibility, and legal/civic questions
   - **(c) Respect for Conduct** — Not blanket rejection; genuine participation in the assessment
   - **(d) Respect for Wisdom & Intelligence** — Demonstrated competence on difficult questions; evidence of sophisticated reasoning

5. **Grader Responsibility:** The grader must interpret all three intellect styles (VA, S, PG) and the IQ estimation as complementary assessments. A gifted individual (180+) who answers non-conformingly is NOT failing — they are operating at a level where conventional scoring does not apply. The Gifted Protocol exists to formally recognize this.

6. **Non-Discrimination:** The system does not penalize unconventional answers that meet the four conditions of respect. A low raw score combined with high deliberateness, high hard-tier accuracy, and respect for law/conduct/wisdom/intelligence triggers formal recognition rather than failure.

## Future Features (Planned)
- [ ] Timer per section
- [ ] Review/change answers before submitting
- [ ] Cumulative scoring across all sections
- [ ] Results history viewer
- [ ] Dark mode theme toggle
- [ ] Export results as PDF
- [ ] Admin password protection
- [ ] Save/load curve presets to file
