I'm designing a mobile app called "Sodatter-BT" for Android.
It's a home gardening management app that tracks crops from seeding to harvest.
The app connects to Bluetooth devices (a digital scale and a mobile printer) and electronic paper tags.


IMPORTANT: Follow the design principles of the Japan Digital Agency Design System (デジタル庁デザインシステム). Reference: https://design.digital.go.jp/dads/


Design principles:
- Clean, highly readable, accessible design prioritizing usability
- White/light neutral background (#FFFFFF or very light warm gray)
- Minimal use of color — NO solid color fills for large areas. Color is used sparingly for borders, outlines, icons, and text accents only
- Typography: Noto Sans JP, clear hierarchy. Body 16px, labels 14px, headings 20-24px
- Spacing: Consistent 8px grid system (8, 16, 24, 32, 40, 48px)
- Corner radius: R4 for small components (≤40px short side), R8 for medium (41-119px), R12 for large (≥120px)
- Cards and containers use thin 1px borders rather than shadows or filled backgrounds
- Buttons: Primary uses outline/border style, not solid fill. Text is the accent color, background stays white
- High contrast ratio (4.5:1 minimum for text, 3:1 for UI components)


Color scheme (used ONLY for borders, outlines, icons, small text accents — NOT as background fills):
- Primary: Soft blue (#5B8BD4) — represents Bluetooth connectivity
- Secondary: Soft green (#6DAE72) — represents plants/growth
- Use these colors for: card borders, icon strokes, active tab indicators, text labels, thin dividers, chip outlines
- Do NOT use these colors as: background fills, large colored areas, solid-colored headers, or filled buttons
- Semantic colors: Success green for harvest/complete states, neutral gray for inactive/empty states
- Error: #EC0000 (per Digital Agency standard)
- Warning: #F2A800


- Background: Pure white (#FFFFFF) or very light gray (#F7F7F7) for section separation
- Text: #1A1A1C (near-black) for body, #6B6B6B for secondary text
- Borders: #D4D4D4 for neutral borders, primary blue or secondary green for active/accent borders


UI style:
- Bottom navigation bar with 3 tabs: Home, History, Settings
- Thin underline or border indicator for active states (not filled backgrounds)
- Cards with 1px borders and white backgrounds — clean and airy
- Form inputs with clear labels above, 1px borders, proper focus states
- The overall aesthetic should feel like a government digital service: trustworthy, accessible, calm, and professional — but adapted for a personal gardening context


Target: Android phone, portrait orientation


Create the Home screen for "Sodatter-BT", a home gardening management app.
Follow the Japan Digital Agency Design System principles described above.


Layout:
- Top: Simple header bar with app name "Sodatter-BT" in regular weight (not bold), left-aligned. A "+" icon button on the right side, using the primary blue (#5B8BD4) outline style
- Main content: 4 cards in a 2x2 grid, each representing a growing device (Device A, B, C, D)


Active device card:
- White background, 1px border in secondary green (#6DAE72)
- Top-left: Device label "A" as a small outlined chip/badge in green
- Crop variety name in body text (16px, near-black)
- Seeding date in secondary text (14px, gray)
- Days elapsed: "Day 32" in primary blue as a small accent
- Small crop thumbnail on the right side (or a simple leaf icon in green outline if no photo)
- No shadow, no colored background fill


Empty device card:
- White background, 1px dashed border in light gray (#D4D4D4)
- Centered: A simple "+" icon in gray with "Tap to register" text below in secondary gray
- Feels clearly empty but not broken


Bottom navigation: 3 tabs — Home (active, indicated by a thin blue underline and blue icon), History (gray icon), Settings (gray icon)


The overall feel should be calm, spacious, and easy to scan at a glance. Plenty of white space.

Create the Seeding Registration screen for "Sodatter-BT".
Follow the Japan Digital Agency Design System principles.

This screen appears when the user taps an empty device card or the "+" button.

Layout:
- Top: Header with a back arrow (gray) and title "New Seeding" in regular weight
- Form fields in a vertical scroll, each with a clear label ABOVE the input (not placeholder-only):

1. Device selector: Horizontal row of outlined chips showing A, B, C, D. Available devices have a 1px green border; already-in-use devices are grayed out with text "In use"
2. "Variety" text input: 1px gray border, R8 corner radius, 48px height
3. "Manufacturer" text input: Same style
4. "Seeding Date" date picker: Shows today's date by default, with a small calendar icon on the right in blue outline
5. "Seed Photo" section: A rectangular area (aspect ratio ~3:2) with a 1px dashed gray border, a camera icon in the center (gray outline), and text "Take a photo (optional)" below it

- Bottom: A single "Register" button — outlined style (1px green border, green text, white background, R8), full width. On hover/press, the border thickens slightly

- Generous vertical spacing between fields (24px between each group)
- All text is near-black (#1A1A1C), labels in 14px, inputs in 16px

Keep it clean and spacious. No decorative elements.

Create the Cultivation Detail screen for "Sodatter-BT".
Follow the Japan Digital Agency Design System principles.

This screen appears when tapping an active device card on Home.

Layout:
- Top: Header with back arrow and crop variety name as title (regular weight, 20px)

- Info section (white card, 1px green border, R12):
- Left: Seed photo thumbnail (80x80, R8) or a leaf icon placeholder
- Right: Variety name (16px, near-black), Manufacturer (14px, gray), Device badge "A" (outlined chip in green)
- Below: Row showing "Seeded: 2026/01/04" and "Day 32" separated by a thin vertical divider

- "Growth Log" section:
- Section header: "Growth Log" (16px, near-black) with a camera icon button on the right (blue outline circle)
- If photos exist: Horizontal scroll of thumbnails (64x64, R8, 1px gray border), date below each in 12px gray
- If no photos: Light gray background area (#F7F7F7) with text "No photos yet. Scan the QR on the device tag to quickly add one." in 14px gray, centered

- Bottom fixed area:
- "Harvest" button: Full width, 1px green border, green text, white background, R8. A small scissors icon in green before the text

No shadow, no color fills. White space between sections (24px).

Create the QR Scan to Photo Record flow for "Sodatter-BT".
Follow the Japan Digital Agency Design System principles.

This is 2 screens shown in sequence:

Screen A — QR Scanner:
- Full-screen camera viewfinder
- Center: A square scan frame with thin 2px blue (#5B8BD4) corner brackets (not a full border — just the 4 corners)
- Top overlay (semi-transparent dark): Text "Scan the tag on your device" in white, 16px
- Bottom: "Cancel" text button in white
- Minimal UI — the camera feed is the focus

Screen B — Quick Photo Record (after QR recognized):
- Top: A compact info bar with white background and 1px bottom border in green
- Content: "Device A — Sunny Lettuce, Day 32" in 14px near-black
- Center: Camera viewfinder / live preview (large area, takes most of the screen)
- Below viewfinder: A thin strip showing the last 2-3 growth photos as small thumbnails (48x48, R4, 1px gray border) for framing reference
- Bottom: Large circular shutter button (white fill, 2px green border, 64px diameter) centered. A "Skip" text link in gray to the left

After photo taken:
- Full-screen photo preview
- Bottom bar: "Save" button (green outline, R8) and "Retake" button (gray outline, R8) side by side
- On save: Brief toast notification "Photo saved" at the bottom

The flow should feel quick and seamless. Minimal chrome, maximum focus on the camera.

Create the Harvest Recording screen for "Sodatter-BT".
Follow the Japan Digital Agency Design System principles.

Layout:
- Top: Header with back arrow and title "Harvest"

- Crop info card (white, 1px gray border, R12):
- Variety name, Device badge, Seeding date, "Day 42" — compact single card

- Weight display section (the focal point):
- Large centered area with generous white space above and below
- Weight value: "--- g" as placeholder, or "142.5 g" when connected
- Typography: 48px, near-black, regular weight (not bold — following Digital Agency's clean style)
- Below the weight: Scale connection status in a single line
- Connected: Small green circle (8px) + "Decent Scale: Connected" in 14px gray
- Not connected: Small gray circle + "Not connected" in 14px light gray
- "Tare" button: Small outlined chip (1px blue border, blue text, R4), positioned to the right of the weight display

- Harvest date: Date field (same style as registration screen), defaulting to today

- Bottom actions (stacked, full width):
- "Complete Harvest" — outlined button (1px green border, green text, white bg, R8)
- "Print Label" — outlined button (1px blue border, blue text, white bg, R8)

The weight number should be the clear visual anchor. Everything else recedes.

Create the Label Print Preview screen for "Sodatter-BT".
Follow the Japan Digital Agency Design System principles.

Layout:
- Top: Header with back arrow and title "Label Preview"

- Label mockup (center of screen):
- A white card with 1px solid gray border, R4 — styled to resemble a physical printed receipt/label
- Inside the label:
- Crop variety name (bold, 18px)
- "Seeded: 2026/01/04 → Harvested: 2026/02/15"
- "Weight: 142.5 g"
- A QR code square (64x64) at the bottom-right corner
- Below the label card: Small text "QR links to the growth photo log" in 12px gray

- Printer status section:
- A single row: Printer icon (blue outline) + "Star SM-S210i" + status indicator (green/gray circle + Connected/Not connected)
- If not connected: A "Connect" text link in blue

- Bottom actions (stacked, full width):
- "Print" — outlined button (1px blue border, blue text, white bg, R8)
- "Done — Return to Home" — outlined button (1px green border, green text, white bg, R8)

The label preview should look realistic and be the visual focus. Simple and centered.

Create the History screen for "Sodatter-BT".
Follow the Japan Digital Agency Design System principles.

This is the second tab in the bottom navigation.

Layout:
- Top: Header with "History" title and a small filter icon (gray outline) on the right

- List of past harvest records, each as a horizontal card:
- White background, 1px gray border on top/bottom (or full card border), R8
- Left accent: A thin 3px left border in green (#6DAE72)
- Layout inside each card:
- Left: Small thumbnail (48x48, R4) of the crop or seed photo
- Center (flex):
- Variety name (16px, near-black)
- Device chip "B" (small outlined badge) + date range "01/04 → 02/15 (42 days)" in 14px gray
- Right: Harvest weight "185g" in 16px, near-black, semi-bold

- Group by month: "February 2026", "January 2026" as section headers (14px, gray, uppercase-style)

- At the bottom of the list:
- A card with dashed border: "Statistics — Coming Soon" with a small bar chart icon in gray
- This teaser card is in light gray (#F7F7F7) background

- Bottom navigation: Home, History (active — blue underline + blue icon), Settings

Clean, scannable list. Consistent card heights for visual rhythm.

Create the Settings screen for "Sodatter-BT".
Follow the Japan Digital Agency Design System principles.

This is the third tab in the bottom navigation.

Layout:
- Top: Header with "Settings" title

- Grouped list sections with clear section headers (14px, gray, all caps or small caps style):

Section 1: "DEVICES"
- 4 rows (Device A, B, C, D)
- Each row: Device label + "Tag: AA:BB:CC:DD:EE:FF" in monospace 14px gray, or "Not assigned" in light gray
- Right side: A small chevron ">" icon in gray
- Rows separated by 1px gray dividers

Section 2: "PERIPHERALS"
- "Digital Scale" row: "Decent Scale" + green/gray status dot + "Connected"/"Not connected"
- "Printer" row: "Star SM-S210i" + status dot + status text
- "ESP32 Access Point" row: IP address or "Not configured" in gray
- Each row has a chevron on the right
- 1px dividers between rows

Section 3: "DATA"
- "Export Data" row with a download icon (blue outline) on the left
- "Cloud Sync" row with a cloud icon (gray outline) + "Coming Soon" chip badge (gray border, gray text, R4)

Section 4: "ABOUT"
- "Version 1.0.0" as plain text
- "Open Source Licenses" as a link in blue with chevron

- Bottom navigation: Home, History, Settings (active — blue underline + blue icon)

Standard settings list pattern. No decorative elements. Clear grouping with section headers.