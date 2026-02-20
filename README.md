# DROM Architecture Visualizer

Animated, interactive architecture diagrams in the browser. Define nodes, connections, and sequence steps in JSON and watch the flow animate across a spatial canvas or a sequence diagram.

## Features

- **Spatial View** — drag-and-drop nodes on a canvas with animated message particles
- **Sequence View** — auto-generated sequence diagram from the same JSON
- **Live JSON Editor** — edit the diagram definition in-browser and see changes instantly
- **Project Notebook** — freeform notes rendered beside the diagram
- **PDF Export** — export spatial view, sequence diagram, notes, and logs to PDF
- **Dark / Light Theme** — toggle with one click
- **Zones** — infrastructure boundary boxes (VPC, subnet, cloud, on-prem, edge, DMZ, etc.)
- **Phases** — progressive reveal slider to show architecture layers (skeleton → processing → observability)
- **Flows** — multiple named animation sequences over the same architecture (happy path, error path, etc.)
- **Step-through Mode** — pause and advance the animation one step at a time
- **JSON Dropdown** — auto-discovers `.json` files from the `json/` folder

## Included Examples

| File | Description |
|------|-------------|
| `ai-agent-collab.json` | Multi-agent orchestration with shared memory |
| `ci-cd-pipeline.json` | Git to production CI/CD flow |
| `ecommerce-checkout.json` | Shopping cart to payment to notification |
| `event-pipeline.json` | Kafka-based real-time data pipeline |
| `iot-sensor-network.json` | Edge sensors with anomaly detection |
| `microservice-migration.json` | Legacy monolith to microservice migration |
| `oauth-auth-flow.json` | OAuth2 / OIDC authentication flow |
| `order-platform.json` | Cloud-native order platform with zones, phases, and flows |
| `rag-pipeline.json` | Retrieval-Augmented Generation AI pipeline |

## Prerequisites

- **Java 17+**

## Quick Start with jbang

[jbang](https://www.jbang.dev/) lets you run the project with a single command — no Maven, no build step.

**Install jbang** (if you don't have it):

```bash
# Linux / macOS
curl -Ls https://sh.jbang.dev | bash -s - app setup

# Windows (Scoop)
scoop install jbang

# Windows (Chocolatey)
choco install jbang
```

**Run the app:**

```bash
jbang ArchViz.java
```

Open your browser at **http://localhost:8080/collab-animation.html**

## Running with Maven

```bash
# Run directly
mvn spring-boot:run

# Or build a fat JAR and run it
mvn package
java -jar target/archviz-0.2.0.jar
```

Then open **http://localhost:8080/collab-animation.html**

## Loading a Specific Diagram

Use the dropdown in the header, or pass a query parameter:

```
http://localhost:8080/collab-animation.html?collab=rag-pipeline.json
```

## Adding Your Own Diagrams

Create a `.json` file in `src/main/resources/static/json/` following this structure:

```json
{
    "title": "My Diagram",
    "notes": "Project: Example\nTeam: My Team\n\nDescription here.",
    "nodes": [
        { "id": "usr", "type": "user", "tag": "external", "label": "User", "x": 50, "y": 200, "w": 100, "h": 70 },
        { "id": "svc", "type": "service", "tag": "core", "label": "Service", "x": 300, "y": 200, "w": 120, "h": 80, "status": "ready" }
    ],
    "connections": [
        { "from": "usr", "to": "svc" }
    ],
    "sequence": [
        { "from": "usr", "to": "svc", "text": "Send request", "status": "ready" },
        { "from": "svc", "to": "usr", "text": "Return response", "status": "ready" }
    ]
}
```

### Node Types (icons)

| Type | Icon | Shape |
|------|------|-------|
| `user` / `human` | Person silhouette | Default rectangle |
| `service` | Stacked layers | Default rectangle |
| `database` | Cylinder | Bottom rounded |
| `agent` | Robot / AI | Default rectangle |
| `gateway` | Globe/network | Circle |
| `firewall` | Shield | Double red border |
| `lambda` | Lambda arrow | Triangle clip-path |
| `fargate` | Container hex | Default rectangle |
| `ec2` | Server rack | Default rectangle |
| `load-balancer` | Balance arrows | Default rectangle |
| `cdn` | Dashed globe | Dashed circle |
| `cache` | Dotted layers | Dotted rounded |
| `queue` | Item queue | Right pill |
| `storage` | Drive stack | Thick bottom border |
| `dashboard` | Grid panels | Top accent bar |
| `vpn` | Shield + check | Double border |

### Node Tags (colors)

| Tag | Color | Use for |
|-----|-------|---------|
| `legacy` | Red | Systems being deprecated |
| `new` | Green | New / target-state components |
| `core` | Blue | Standard infrastructure |
| `agent` | Purple | AI agents / LLMs |
| `external` | Gray (dashed) | Users or third-party systems |
| `aws` | Orange | AWS-managed services |
| `internal` | Teal | Internal / on-prem services |
| `observability` | Purple | Monitoring, logging, dashboards |
| `data` | Cyan | Databases, caches, data stores |

### Optional Node Fields

| Field | Description |
|-------|-------------|
| `status` | `"ready"` (green check) or `"wip"` (orange hourglass) |
| `skipSequence` | `true` to hide from sequence diagram (e.g. databases) |
| `phase` | Phase ID — node only appears when that phase is selected |

### Zones (infrastructure boundaries)

Zones draw labeled boundary boxes behind nodes to represent infrastructure groupings. Add a `"zones"` array to your JSON:

```json
"zones": [
    { "id": "cloud", "type": "cloud", "label": "AWS Cloud", "x": 200, "y": 10, "w": 800, "h": 500 },
    { "id": "vpc", "type": "vpc", "label": "VPC", "x": 220, "y": 40, "w": 760, "h": 460, "parent": "cloud" }
]
```

| Zone Type | Style | Use for |
|-----------|-------|---------|
| `cloud` | Blue dashed | Cloud provider boundary |
| `on-prem` | Gray solid | On-premises datacenter |
| `vpc` | Green dashed | Virtual private cloud |
| `subnet` | Green dotted | Subnet within a VPC |
| `edge` | Yellow dashed | Edge / CDN layer |
| `dmz` | Red solid | Demilitarized zone |
| `region` / `az` | Subtle blue | AWS region or availability zone |
| `k8s-cluster` | Blue solid | Kubernetes cluster |
| `namespace` | Blue dashed | Kubernetes namespace |

### Phases (progressive architecture reveal)

Phases let you reveal architecture in layers, like building blueprints — skeleton first, then plumbing, then facade. Add a `"phases"` array and tag items with a `"phase"` field:

```json
"phases": [
    { "id": "skeleton", "label": "1 — Skeleton" },
    { "id": "processing", "label": "2 — Processing" },
    { "id": "observability", "label": "3 — Observability" }
]
```

Then tag nodes, connections, zones, and sequence steps with `"phase": "processing"` etc. Items without a `phase` field are always visible. The slider in the header controls which phase level is shown — all items with a phase index up to the selected one are displayed.

### Flows (named animation sequences)

Flows let you define multiple named animation paths through the same architecture. Add a `"flows"` array:

```json
"flows": [
    {
        "id": "happy-path",
        "name": "Happy Path — Place Order",
        "sequence": [
            { "from": "user", "to": "gw", "text": "POST /orders", "status": "ready" },
            { "from": "gw", "to": "svc", "text": "Forward request", "status": "ready" }
        ]
    },
    {
        "id": "error-path",
        "name": "Payment Failure",
        "sequence": [...]
    }
]
```

A dropdown appears in the header when flows are present. The root `"sequence"` array is used as the default. Each flow's sequence is independently filtered by the current phase.

## Performing an Architecture Review

ArchViz is designed to support structured architecture reviews where you walk a team through both the **static architecture** and the **dynamic flows**.

### 1. Start with the spatial view (architecture diagram)

Load your diagram and use the **Phase slider** to reveal the architecture layer by layer:

- **Phase 1** — show the core skeleton (API, database, key services)
- **Phase 2** — add async processing (queues, caches, workers)
- **Phase 3** — add observability, CDN, admin tooling

At each phase, discuss:
- What infrastructure boundaries (zones) are in play?
- Which components are `ready` vs `wip`?
- Are connections between layers clear?

### 2. Walk through flows (sequence view)

Switch to **Sequence View** and use the **Flow dropdown** to animate each request path:

- **Happy path** — does the ideal flow make sense? Are there unnecessary hops?
- **Error paths** — what happens on payment failure, timeout, or service unavailability?
- **Cache/optimization paths** — does the caching strategy cover the right scenarios?

Use **Pause/Step** mode to discuss each step individually. The log pane records every step with timestamps.

### 3. Combine both views

For each flow, toggle between spatial and sequence views:
- The **spatial view** shows *where* messages travel across infrastructure
- The **sequence view** shows *when* and *in what order* interactions happen

Both views respect the current phase, so you can review a flow at different maturity levels (e.g., "what does the happy path look like before we add caching?").

### 4. Export for offline review

Use **Export PDF** to capture the current state (respects the active phase) for sharing with stakeholders who can't attend live.

## Persistence

By default the app stores diagrams in memory (lost on restart). To use **Amazon DynamoDB** for persistent storage:

### 1. Switch the store in `application.properties`

```properties
# Change from inMemory to dynamodb
diagram.store=dynamodb
```

### 2. Configure AWS region and table name (optional)

The defaults are `us-east-1` and `archviz-diagrams`. Override in `application.properties` if needed:

```properties
aws.region=us-east-1
aws.dynamodb.table-name=archviz-diagrams
```

### 3. Provide AWS credentials

Credentials are resolved via the standard AWS credential chain:

- Environment variables: `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`
- `~/.aws/credentials` profile file
- IAM role (when running on EC2/ECS/Lambda)

### 4. Create the DynamoDB table

The table only requires `id` (String) as the partition key. All other attributes are stored automatically.

```bash
aws dynamodb create-table \
  --table-name archviz-diagrams \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST
```

That's it — start the app with `mvn spring-boot:run` and diagrams saved via the UI will persist in DynamoDB.

## Project Structure

```
.
├── ArchViz.java                             # jbang single-file runner
├── pom.xml                                  # Maven build (Spring Boot 3.4.2, Java 17)
├── src/main/java/io/github/drompincen/archviz/
│   ├── ArchVizApplication.java              # Spring Boot main class
│   ├── JsonListController.java              # GET /json/ directory listing
│   └── HtmlAccessLogFilter.java             # Logs every .html page access
├── src/main/resources/
│   ├── application.properties
│   └── static/
│       ├── animation.html                   # Phase animator
│       ├── collab-animation.html            # Collaboration animator (main)
│       └── json/                            # Example diagram definitions
└── README.md
```

## License

MIT License — see [license.txt](license.txt).
