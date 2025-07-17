# Use Case Diagram - Unit Converter System

```mermaid
graph TB
    %% Actors
    USER[👤 User]
    ADMIN[👨‍💼 System Administrator]
    
    %% System Boundary
    subgraph "Unit Converter System"
        %% Primary Use Cases
        UC1[📏 Convert Length Units]
        UC2[🌡️ Convert Temperature Units]
        UC3[⚖️ Convert Weight Units]
        UC4[📋 View Supported Units]
        UC5[🔍 Validate Input Values]
        UC6[📊 Get Conversion History]
        
        %% Secondary Use Cases
        UC7[⚠️ Handle Conversion Errors]
        UC8[🔧 Configure Unit Preferences]
        UC9[📱 Access Mobile Interface]
        UC10[💾 Save Conversion Results]
        
        %% Administrative Use Cases
        UC11[📈 Monitor System Usage]
        UC12[⚙️ Manage Configuration]
        UC13[🔄 Update Conversion Factors]
        UC14[📝 View System Logs]
    end
    
    %% External Systems
    BROWSER[🌐 Web Browser]
    DEPLOY[☁️ Deployment Platform]
    
    %% User Relationships
    USER --> UC1
    USER --> UC2
    USER --> UC3
    USER --> UC4
    USER --> UC5
    USER --> UC6
    USER --> UC8
    USER --> UC9
    USER --> UC10
    
    %% Admin Relationships
    ADMIN --> UC11
    ADMIN --> UC12
    ADMIN --> UC13
    ADMIN --> UC14
    
    %% System Relationships
    UC1 ..> UC5 : includes
    UC2 ..> UC5 : includes
    UC3 ..> UC5 : includes
    UC1 ..> UC7 : extends
    UC2 ..> UC7 : extends
    UC3 ..> UC7 : extends
    UC6 ..> UC10 : extends
    
    %% External Dependencies
    USER -.-> BROWSER : uses
    BROWSER -.-> UC1 : accesses
    BROWSER -.-> UC2 : accesses
    BROWSER -.-> UC3 : accesses
    DEPLOY -.-> UC11 : provides metrics
    
    %% Styling
    classDef actor fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    classDef usecase fill:#f1f8e9,stroke:#388e3c,stroke-width:2px
    classDef admin fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    classDef external fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    
    class USER actor
    class ADMIN admin
    class UC1,UC2,UC3,UC4,UC5,UC6,UC7,UC8,UC9,UC10 usecase
    class UC11,UC12,UC13,UC14 admin
    class BROWSER,DEPLOY external
```

## Use Case Descriptions

### Primary Use Cases (User)

#### UC1: Convert Length Units
- **Actor**: User
- **Goal**: Convert a value from one length unit to another
- **Preconditions**: User has access to the system
- **Main Flow**:
  1. User selects length conversion page
  2. User enters numeric value
  3. User selects source unit (e.g., meters)
  4. User selects target unit (e.g., feet)
  5. System validates input
  6. System performs conversion calculation
  7. System displays converted value
- **Extensions**: 
  - Invalid input → Handle Conversion Errors
- **Postconditions**: Conversion result is displayed to user

#### UC2: Convert Temperature Units
- **Actor**: User
- **Goal**: Convert a temperature value between different scales
- **Preconditions**: User has access to the system
- **Main Flow**:
  1. User selects temperature conversion page
  2. User enters temperature value
  3. User selects source scale (Celsius, Fahrenheit, Kelvin)
  4. User selects target scale
  5. System validates temperature range
  6. System applies conversion formula
  7. System displays converted temperature
- **Extensions**: 
  - Temperature below absolute zero → Handle Conversion Errors
- **Postconditions**: Temperature conversion result is displayed

#### UC3: Convert Weight Units
- **Actor**: User
- **Goal**: Convert a weight/mass value between different units
- **Preconditions**: User has access to the system
- **Main Flow**:
  1. User selects weight conversion page
  2. User enters weight value
  3. User selects source unit (kg, lbs, oz, etc.)
  4. User selects target unit
  5. System validates positive value
  6. System calculates conversion using factors
  7. System displays converted weight
- **Extensions**: 
  - Negative weight value → Handle Conversion Errors
- **Postconditions**: Weight conversion result is displayed

#### UC4: View Supported Units
- **Actor**: User
- **Goal**: See all available units for each conversion type
- **Preconditions**: User has access to the system
- **Main Flow**:
  1. User requests supported units list
  2. System retrieves units from repository
  3. System displays categorized unit lists
  4. User can view unit abbreviations and full names
- **Postconditions**: Complete unit listing is displayed

#### UC5: Validate Input Values
- **Actor**: System (included by conversion use cases)
- **Goal**: Ensure input data is valid before conversion
- **Main Flow**:
  1. System receives input data
  2. System validates numeric format
  3. System checks unit validity
  4. System verifies value constraints
  5. System confirms data integrity
- **Extensions**: 
  - Invalid data → Throw validation error
- **Postconditions**: Input is validated or error is raised

#### UC6: Get Conversion History
- **Actor**: User
- **Goal**: View previously performed conversions
- **Preconditions**: User has performed conversions
- **Main Flow**:
  1. User requests conversion history
  2. System retrieves stored conversions
  3. System displays chronological list
  4. User can review past calculations
- **Extensions**: 
  - Save results → UC10
- **Postconditions**: Conversion history is displayed

### Secondary Use Cases

#### UC7: Handle Conversion Errors
- **Actor**: System
- **Goal**: Gracefully handle and display conversion errors
- **Main Flow**:
  1. System detects error condition
  2. System determines error type
  3. System formats user-friendly error message
  4. System displays error to user
  5. System suggests corrective action
- **Postconditions**: Error is communicated clearly to user

#### UC8: Configure Unit Preferences
- **Actor**: User
- **Goal**: Set default or preferred units for conversions
- **Main Flow**:
  1. User accesses settings/preferences
  2. User selects default units per category
  3. System saves user preferences
  4. System applies preferences to future conversions
- **Postconditions**: User preferences are saved and applied

#### UC9: Access Mobile Interface
- **Actor**: User
- **Goal**: Use the converter on mobile devices
- **Main Flow**:
  1. User accesses system via mobile browser
  2. System detects mobile device
  3. System provides responsive mobile interface
  4. User performs conversions with touch interface
- **Postconditions**: Mobile-optimized interface is provided

#### UC10: Save Conversion Results
- **Actor**: User
- **Goal**: Save or export conversion results
- **Main Flow**:
  1. User performs conversion
  2. User requests to save result
  3. System formats result for storage/export
  4. System provides save/download option
  5. Result is saved locally or downloaded
- **Postconditions**: Conversion result is saved/exported

### Administrative Use Cases

#### UC11: Monitor System Usage
- **Actor**: System Administrator
- **Goal**: Track system performance and usage metrics
- **Main Flow**:
  1. Admin accesses monitoring dashboard
  2. System displays usage statistics
  3. Admin reviews performance metrics
  4. Admin identifies usage patterns
- **Postconditions**: System usage insights are provided

#### UC12: Manage Configuration
- **Actor**: System Administrator
- **Goal**: Update system settings and configuration
- **Main Flow**:
  1. Admin accesses configuration interface
  2. Admin modifies system settings
  3. System validates configuration changes
  4. System applies new configuration
- **Postconditions**: System configuration is updated

#### UC13: Update Conversion Factors
- **Actor**: System Administrator
- **Goal**: Maintain accuracy of conversion calculations
- **Main Flow**:
  1. Admin accesses conversion factor repository
  2. Admin updates or adds conversion factors
  3. System validates factor accuracy
  4. System deploys updated factors
- **Postconditions**: Conversion factors are updated

#### UC14: View System Logs
- **Actor**: System Administrator
- **Goal**: Monitor system health and troubleshoot issues
- **Main Flow**:
  1. Admin accesses system logs
  2. System displays error logs and activities
  3. Admin analyzes log entries
  4. Admin identifies issues or patterns
- **Postconditions**: System logs are reviewed

## Use Case Relationships

### Include Relationships
- All conversion use cases include input validation
- Input validation ensures data integrity before processing

### Extend Relationships
- Conversion use cases extend to error handling when issues occur
- Conversion history extends to save results functionality

### Actor Interactions
- **User**: Primary system user performing conversions
- **System Administrator**: Manages system configuration and monitoring
- **Web Browser**: External interface for user access
- **Deployment Platform**: External system providing hosting and metrics