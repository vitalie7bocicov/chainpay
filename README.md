# ChainPay: Blockchain Transaction System

This project is part of a Java Developer course where 
I implemented a blockchain transaction system to deepen my understanding of core Java concepts and transaction 
handling over a blockchain network.

## Overview

**ChainPay** is a blockchain system that handles transactions and stores a chain of blocks, 
each containing a unique identifier, a timestamp, and a hash of the previous block. 
The chain starts with a block whose ID is `1`, and the first block has a previous block hash of `0`.

Each new block in the chain is created by miners, who attempt to solve a cryptographic puzzle that involves finding a 
block hash that starts with a specific number of zeros. This number is referred to as `N`, and it is adjustable based 
on the time taken to mine a new block.

## Features

- **Blockchain Implementation**: Each block contains a timestamp, a unique ID, the hash of the previous block, and a cryptographic 
hash of its contents (using SHA-256).
- **Dynamic Difficulty Adjustment**: The number of leading zeros (`N`) required for 
the block hash adjusts dynamically based on mining speed.
- **Message Handling**: Miners work on creating blocks, 
while users send messages that are included in the new block once it is mined.
- **Transaction System**: Instead of messages, this version includes transactions, where each participant has a starting 
balance of 100 virtual coins, and coins are awarded for mining blocks.

## Getting Started

### Prerequisites

- Java 21 or later
- Any IDE or command-line setup for Java development

### Installation

1. Clone the repository:
    ```bash
    git clone https://github.com/vitalie7bocicov/chainpay
    ```

2. Compile the code:
    ```bash
    javac Blockchain.java
    ```

3. Run the main class:
    ```bash
    java Blockchain
    ```

### Running the Program

- The program will generate new blocks based on the mining process.
- You can observe the output for the first 5 blocks and the time taken to create each block.
- Users can send transactions (starting with 100 virtual coins) that will be included in the next mined block.
- Miners solve the cryptographic puzzle by finding a block hash that starts with `N` zeros, which adjusts dynamically.

## Features Implemented

### Stage 1: Basic Blockchain Implementation

- Each block has a unique ID, a timestamp, and a previous block hash.
- The first block starts with ID 1, and its previous hash is set to `0`.
- Blocks are created using SHA-256 hashing.

### Stage 2: Difficulty Adjustment

- The blockchain now requires new blocks to have hashes starting with `N` zeros, where `N` is adjustable.
- The mining difficulty increases or decreases based on the time taken to create a block.

### Stage 3: Multiple Miners

- Multiple miners work concurrently, each trying to mine a new block.
- The blockchain adjusts the difficulty based on the speed of block creation.

### Stage 4: Message Handling

- Users can send messages to the blockchain, and once a new block is mined, those messages are included in the block.
- Messages are added to the list of pending messages and are only included in the block once it is mined.

### Stage 5: Message Validation

- Messages now include a text, signature, unique identifier, and public key.
- The blockchain rejects messages with identifiers lower than the maximum identifier of the previous block.
- Blockchain validation also ensures that the messages have valid signatures.

### Stage 6: Transaction System

- Instead of messages, the blockchain now handles transactions, where each user starts with 100 virtual coins.
- Transactions are included in the blocks, and users’ coin balances are updated as blocks are mined.

## Example Output

### Example of blockchain output:

```
Available processord: 8
miner1 searching for block: 1
miner2 searching for block: 1
miner6 searching for block: 1
miner7 searching for block: 1
miner5 searching for block: 1
miner4 searching for block: 1
miner0 searching for block: 1
miner3 searching for block: 1

Block:
Created by miner2
miner2 gets 100 VC
Id: 1
Timestamp: 1737277333084
Magic number: 4194276582641332057
Hash of the previous block: 
0
Hash of the block: 
9c919015faa3b5c0f2527de02aaa8956d3acf74bcbec51f0b6c1d2a02bcca688
Block data: 
No transactions
Block was generating for 0 seconds
N was increased to 1
miner2 searching for block: 2
miner6 searching for block: 2
miner5 searching for block: 2
miner3 searching for block: 2
miner7 searching for block: 2
miner0 searching for block: 2

Block:
Created by miner6
miner6 gets 100 VC
Id: 2
Timestamp: 1737277333164
Magic number: 8240922226183644301
Hash of the previous block: 
9c919015faa3b5c0f2527de02aaa8956d3acf74bcbec51f0b6c1d2a02bcca688
Hash of the block: 
07e297a1e09419ab437cd8a75985f4ea0749461ee3f59fb2e30c47a475e0e483
Block data: 
No transactions
Block was generating for 0 seconds
N was increased to 2
miner6 searching for block: 3
miner4 searching for block: 3
miner5 searching for block: 3
miner7 searching for block: 3
miner2 searching for block: 3
miner1 searching for block: 3
miner0 searching for block: 3
miner3 searching for block: 3

Block:
Created by miner7
miner7 gets 100 VC
Id: 3
Timestamp: 1737277333176
Magic number: 1404750850799846900
Hash of the previous block: 
07e297a1e09419ab437cd8a75985f4ea0749461ee3f59fb2e30c47a475e0e483
Hash of the block: 
00ae95a56c77654562c5647bd82441c5c5344cb18c67950ff096e920eda671da
Block data: 
No transactions
Block was generating for 0 seconds
N was increased to 3
miner7 searching for block: 4
miner5 searching for block: 4
miner0 searching for block: 4
miner3 searching for block: 4
miner1 searching for block: 4
miner4 searching for block: 4
miner6 searching for block: 4
miner2 searching for block: 4

Block:
Created by miner6
miner6 gets 100 VC
Id: 4
Timestamp: 1737277333202
Magic number: -2623110646414531008
Hash of the previous block: 
00ae95a56c77654562c5647bd82441c5c5344cb18c67950ff096e920eda671da
Hash of the block: 
0007643212d2fa3c2b4ff1f7545cf76f76edb091a7ce520a44e7c4133af617fb
Block data: 
No transactions
Block was generating for 0 seconds
N was increased to 4
miner3 searching for block: 5
miner4 searching for block: 5
miner6 searching for block: 5
miner0 searching for block: 5
miner1 searching for block: 5
miner5 searching for block: 5
miner2 searching for block: 5
SENDING
SENDING
miner7 searching for block: 5

Block:
Created by miner2
miner2 gets 100 VC
Id: 5
Timestamp: 1737277333367
Magic number: -2226705675446246153
Hash of the previous block: 
0007643212d2fa3c2b4ff1f7545cf76f76edb091a7ce520a44e7c4133af617fb
Hash of the block: 
0000c73ccc2ef4552caaef1d6a15ee358e15e521ea352cea83e0bd6ec7ab8621
Block data: 
No transactions
Block was generating for 1 seconds
N was increased to 5
miner0 searching for block: 6
miner3 searching for block: 6
miner4 searching for block: 6
miner2 searching for block: 6
miner7 searching for block: 6
miner6 searching for block: 6
miner1 searching for block: 6
miner5 searching for block: 6
SENDING

Block:
Created by miner4
miner4 gets 100 VC
Id: 6
Timestamp: 1737277334693
Magic number: -784927000738465860
Hash of the previous block: 
0000c73ccc2ef4552caaef1d6a15ee358e15e521ea352cea83e0bd6ec7ab8621
Hash of the block: 
00000be81c861b48ed20c75500dd6288ce25f335e0ded80918d768ca36951a11
Block data: [
 ID: 1 : miner2 send 24 VC to miner9 
, 
 ID: 2 : miner6 send 10 VC to miner1 
]
Block was generating for 3 seconds
N was increased to 6
miner4 searching for block: 7
miner0 searching for block: 7
miner6 searching for block: 7
miner5 searching for block: 7
miner1 searching for block: 7
miner2 searching for block: 7
miner3 searching for block: 7
miner7 searching for block: 7

```
