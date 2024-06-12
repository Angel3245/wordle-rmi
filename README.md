Wordle Implementation using Java RMI in a Client-Server Architecture
===

This project is a Java-based implementation of the popular word-guessing game Wordle. The application uses Java RMI (Remote Method Invocation) to facilitate communication between clients and a server in a client-server architecture. The game supports a command-line interface and allows for multi-language word support.

## Features
- Client-Server Architecture: Uses Java RMI for communication.
- Command-Line Interface: Simple text-based interface for interaction.
- Multi-Language Support: Supports words in different languages.
- Multi-User Support: Multiple clients can connect and play simultaneously.

## Dependencies

- Java Development Kit (JDK) 8 or higher

## Installation and Setup
- Clone the repository:

```sh
git clone https://github.com/Angel3245/wordle-rmi.git
cd wordle-rmi
```

- Build the project:

Windows:

```sh
./compile_all.ps1
```

Linux:

```sh
./compile_all.sh
```

- Run the server:

```sh
java rmiserver.Server <server_ip> <server_port> <server_name>
```

- Run the client/s (human):

```sh
java rmiclient.Client <server_ip> <server_port> <client_name> <language>
```

- Run the client/s (bots):
```sh
java rmiclient.Bot <server_ip> <server_port> <client_name> <language> <strategy>
```

## Supported languages

The game supports multiple languages by loading word lists from text files. The language can be selected when starting the client and the supported languages are:

```
afrikaans, english, hungarian, polish, slovenian, albanian, esperanto. indonesian, portuguese, spanish, catalan, estonian, italian, romanian, czech, finnish, latvian, swedish, danish, french, lithuanian, serbian, turkish, dutch, german, norwegian, slovak, vietnamese
```

## Bot Implementation
To enhance the gameplay experience, we implemented a bot capable of playing Wordle using three different strategies:

- Correct Words in Correct Positions (strategy: 0)
The bot first observes the correct letters in the correct positions based on feedback from previous guesses. This strategy aims to narrow down the possible solutions by ensuring that any correct letters identified are retained in their precise positions in subsequent guesses.

- Words Contained in Solution (strategy: 1)
In this strategy, the bot focuses on identifying letters that are contained in the solution but are not necessarily in the correct position. By iteratively refining guesses to include these letters, the bot can progressively eliminate incorrect possibilities and home in on the correct solution.

- Random Words (strategy: 2)
As a fallback strategy, the bot may guess random words from the dictionary. This approach can sometimes help when the other strategies reach a stalemate or when initial guesses are being made to gather information about the solution.

The strategy can be selected when starting the bot client.

## Implementation Details
The implementation includes:

- Server Initialization: Setting up the RMI registry and binding the server implementation.
- Client Connection: Connecting to the RMI registry and obtaining a reference to the server.
- Game Logic: Managing the game state, validating guesses, and providing feedback to the client.
- Multi-Language Words: Loading word lists from files and selecting the appropriate list based on the user's choice.