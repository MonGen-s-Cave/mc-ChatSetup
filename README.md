# EasierChatSetup

A Paper Minecraft plugin library for easily creating chat-based input systems. This lightweight utility makes it simple to prompt players for text input with rich features like timeouts, validation, and callbacks.

## Features

- 🎮 **Simple builder pattern** - Easy to use fluent API
- ⏱️ **Timeouts** - Set time limits for player responses
- ✅ **Input validation** - Filter responses with custom validators
- 📋 **Multiple callbacks** - Handle success, failure, and input events
- 🔄 **Collection filtering** - Only listen to specific players
- 🎨 **MiniMessage format** - Rich text formatting with placeholders
- 🧹 **Automatic cleanup** - Resources are managed for you

## Installation

### Maven

```xml
<repositories>
    <repository>
        <id>mongenscave-releases</id>
        <url>https://repo.mongenscave.com/releases</url>
    </repository>
</repositories>


<dependencies>
    <dependency>
        <groupId>com.mongenscave</groupId>
        <artifactId>mc-ChatSetup</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy
repositories {
    mavenCentral()
    maven {
        maven("https://repo.mongenscave.com/releases")
    }
}

dependencies {
    implementation("com.mongenscave:mc-ChatSetup:1.0.0")
}
```

## Basic Usage Examples

### Simple Input Prompt

```java
// Create a new chat input session
EasierChatSetup.empty(plugin)
    .addPlayer(player)
    .append("&aEnter a value within {time} seconds! &cType &4{cancel} &cto cancel")
    .setTime(10) // 10 seconds timeout
    .onInput(input -> {
        player.sendMessage("You entered: " + input);
    })
    .onSuccess(() -> {
        player.sendMessage("Thanks for your input!");
    })
    .onFail(() -> {
        player.sendMessage("You didn't enter anything in time.");
    })
    .build();
```

### Direct Instance Method

```java
// Using direct instantiation
EasierChatSetup setup = new EasierChatSetup(plugin);
setup.append("&aEnter your answer: (You have {time}s)")
     .setTime(15)
     .onSuccess(() -> {
         // Handle successful input
     })
     .startSession(player); // Quick start with a player
```

### With Validation

```java
EasierChatSetup.empty(plugin)
    .addPlayer(player)
    .append("&aPlease enter a number between 1 and 100:")
    .withValidator(input -> {
        try {
            int number = Integer.parseInt(input);
            return number >= 1 && number <= 100;
        } catch (NumberFormatException e) {
            player.sendMessage("Please enter a valid number!");
            return false;
        }
    })
    .onInput(input -> {
        int number = Integer.parseInt(input);
        player.sendMessage("You entered: " + number);
    })
    .build();
```

## Advanced Examples

### Custom Cancel Command

```java
EasierChatSetup.empty(plugin)
    .addPlayer(player)
    .append("&aEnter your message. Type &e{cancel} &ato cancel.")
    .setCancel("quit")
    .onSuccess(() -> {
        player.sendMessage("Message received!");
    })
    .onFail(() -> {
        player.sendMessage("Operation cancelled.");
    })
    .build();
```

### Using onStart Callback

```java
EasierChatSetup.empty(plugin)
    .addPlayer(player)
    .append("&aEnter the code sequence:")
    .onStart(() -> {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        player.showTitle(Title.title(
            Component.text("Code Input"),
            Component.text("Enter the secret code")
        ));
    })
    .onSuccess(() -> {
        player.sendMessage("Code accepted!");
    })
    .build();
```

### Filtering Players with Collections

```java
// Only listen to players in a specific collection
Set<Player> authorizedPlayers = getAuthorizedPlayers();

EasierChatSetup.empty(plugin)
    .addPlayer(player1)
    .addPlayer(player2)
    .addPlayer(player3)
    .listenTo(authorizedPlayers) // Only players in this collection will be processed
    .append("&aAuthorized personnel only: Enter your access code")
    .build();
```

### Creating a Number Input System

```java
public int promptForNumber(Player player, String message, int min, int max) {
    final int[] result = {-1};
    
    EasierChatSetup setup = new EasierChatSetup(plugin);
    setup.addPlayer(player)
         .append(message)
         .withValidator(input -> {
             try {
                 int value = Integer.parseInt(input);
                 boolean valid = value >= min && value <= max;
                 if (!valid) {
                     player.sendMessage("Number must be between " + min + " and " + max);
                 }
                 return valid;
             } catch (NumberFormatException e) {
                 player.sendMessage("Please enter a valid number");
                 return false;
             }
         })
         .onInput(input -> result[0] = Integer.parseInt(input))
         .build();
    
    // Note: This is just a demonstration. In real code you'd need
    // to handle the asynchronous nature of this interaction
    return result[0];
}
```

### Multi-step Chat Forms

```java
public void startRegistrationForm(Player player) {
    final String[] username = {null};
    final String[] email = {null};
    
    // Step 1: Ask for username
    EasierChatSetup usernamePrompt = new EasierChatSetup(plugin);
    usernamePrompt.addPlayer(player)
                  .append("&aStep 1/2: Enter your desired username:")
                  .onInput(input -> username[0] = input)
                  .onSuccess(() -> {
                      // Step 2: Ask for email
                      EasierChatSetup emailPrompt = new EasierChatSetup(plugin);
                      emailPrompt.addPlayer(player)
                                .append("&aStep 2/2: Enter your email address:")
                                .onInput(input -> email[0] = input)
                                .onSuccess(() -> {
                                    // Registration complete
                                    player.sendMessage("Registration complete!");
                                    registerUser(username[0], email[0]);
                                })
                                .build();
                  })
                  .build();
}
```

## API Reference

### Core Methods

| Method | Description |
|--------|-------------|
| `EasierChatSetup(JavaPlugin plugin)` | Constructor with plugin reference |
| `static EasierChatSetup empty()` | Static factory method |
| `static EasierChatSetup empty(JavaPlugin plugin)` | Static factory with explicit plugin |
| `addPlayer(Player player)` | Add a player to listen for input |
| `append(String message)` | Set the message to show to players |
| `build()` | Build and start the chat input process |
| `startSession(Player player)` | Quick method to add player and build |

### Configuration Methods

| Method | Description |
|--------|-------------|
| `setTime(int seconds)` | Set timeout in seconds |
| `setTime(Duration duration)` | Set timeout with Duration |
| `setCancel(String cancelCommand)` | Set the cancel command |
| `listenTo(Collection<?> collection)` | Filter players by collection |
| `withValidator(Predicate<String> validator)` | Set input validator |

### Callback Methods

| Method | Description |
|--------|-------------|
| `onStart(Runnable onStart)` | Called when the chat input starts |
| `onInput(Consumer<String> onInput)` | Called when valid input is received |
| `onSuccess(Runnable onSuccess)` | Called on successful completion |
| `onFail(Runnable onFail)` | Called on failure or timeout |

## Message Placeholders

The message text supports the following placeholders:

| Placeholder | Description |
|-------------|-------------|
| `{time}` | Replaced with the timeout in seconds |
| `{cancel}` | Replaced with the cancel command |

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Author

Created by Coma112