Collab Animator — Icons, Node Styles, and JSON Schema
=====================================================

Overview
- This document describes the node icons, visual styles, and the JSON schema used by collab-animation.html.
- Files can be authored inline (the built-in demo) or as external JSON files in the json/ folder. You can load a file by selecting it in the dropdown or via URL: collab-animation.html?collab=FILENAME.json

Icons (by node type)
- user: Person silhouette
- service: Stacked service/box glyph
- database: Database cylinder
- agent: Agent/bot glyph
- default: Solid square (fallback when type is unknown)

Node Styles (spatial view)
- type- classes affect shape details
  - .type-database: squared top with rounded bottom (cylinder feel), thicker top border
- tag- classes color-code the node and border. Colors adapt to theme (dark/light) via CSS variables:
  - .tag-legacy → bg: var(--tag-legacy-bg), border: var(--tag-legacy-border), text: var(--tag-legacy-text)
  - .tag-new → bg: var(--tag-new-bg), border: var(--tag-new-border), text: var(--tag-new-text)
  - .tag-core → bg: var(--tag-core-bg), border: var(--tag-core-border), text: var(--tag-core-text)
  - .tag-agent → bg: var(--tag-agent-bg), border: var(--tag-agent-border), text: var(--tag-agent-text)
  - .tag-external → bg: var(--tag-external-bg), border: var(--tag-external-border), text: var(--tag-external-text), dashed border
- Status badges on nodes (optional)
  - status: "ready" → green checkmark (✔)
  - status: "wip" → orange hourglass (⏳)
- Connectors (spatial view)
  - Lines are drawn between nodes listed in connections
  - Bidirectional pairs (A→B and B→A) are deduplicated and rendered as a single connector
  - Parallel connectors auto-offset to avoid overlaps

Sequence View styling
- Each lifeline header inherits the node’s tag- color variables
- Optional node status appears as a small colored badge in the header
- Nodes with skipSequence: true are hidden to reduce clutter (e.g., databases)

Logs & Notebook
- Notebook displays the top-level notes field with word wrapping (newlines supported)
- Export to PDF includes notebook and log pane

JSON Schema (informal)
Top-level object fields:
- title: string (optional)
- notes: string (optional)
  - Supports literal \n escapes and also tolerates IDE-wrapped multiline strings inside quotes (they are normalized to \n)
- nodes: Node[] (required for diagrams)
- connections: Connection[] (optional; used for spatial connectors and particle animation)
- sequence: SequenceStep[] (optional; used for sequence view and dual-view animation)

Node
{
  id: string,                 // unique identifier (referenced by connections/sequence)
  type?: "user" | "service" | "database" | "agent" | string,  // unknown types use default icon
  tag?: "legacy" | "new" | "core" | "agent" | "external" | string,
  label?: string,             // supports "\n" to line-break
  x: number, y: number,       // position on canvas (px)
  w?: number, h?: number,     // optional size (px)
  status?: "ready" | "wip",  // optional status badge
  skipSequence?: boolean      // hide this lifeline in sequence view
}

Connection
{
  from: string,   // node id
  to: string      // node id
}
- Note: If both {from:A,to:B} and {from:B,to:A} exist, only one connector is drawn in spatial view.

SequenceStep
{
  from: string,                 // node id (sender)
  to: string,                   // node id (receiver)
  text?: string,                // label above the arrow
  status?: "ready" | "wip"     // optional status (affects arrow badge/coloring)
}

Authoring Tips
- Use tag values to communicate domain/phase: legacy, core, new, agent, external
- Prefer concise labels; use "\n" to break onto multiple lines (e.g., "API\nGateway")
- Hide infrastructure-like nodes from sequence view with skipSequence: true
- Keep ids stable across edits; they are the join keys for connections and steps

Loading External Examples
- Place files in json/ and use: collab-animation.html?collab=your-file.json
- The dropdown auto-discovers *.json in json/ (server must allow directory listing)

Version
- This document reflects collab-animation.html as of PR #3 (dedup connectors, multiline notes) and PR #2 (dynamic JSON dropdown).
