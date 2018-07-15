# Blockchain
Assignments from the **Coursera course "Bitcoin and Cryptocurrency Technologies"**

You will be responsible for creating a file called **BlockChain.java** that implements the following API:
```java
// Block Chain should maintain only limited block nodes to 
// satisfy the functions. You should not have all the blocks 
// added to the block chain in memory as it would cause a 
// memory overflow.

public class BlockChain {

  public static final int CUT_OFF_AGE = 10;

  /**
    * create an empty block chain with just a genesis block. 
    * Assume {@code genesisBlock} is a valid block
    */
  public BlockChain(Block genesisBlock) {
    // IMPLEMENT THIS
  }

  /** Get the maximum height block */
  public Block getMaxHeightBlock() {
    // IMPLEMENT THIS
  }

  /** 
    * Get the UTXOPool for mining a new block on top of max 
    * height block 
    */
  public UTXOPool getMaxHeightUTXOPool() {
    // IMPLEMENT THIS
  }

  /** Get the transaction pool to mine a new block */
  public TransactionPool getTransactionPool() {
    // IMPLEMENT THIS
  }

  /**
    * Add {@code block} to the block chain if it is valid. For 
    * validity, all transactions should be valid and block 
    * should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
    * For example, you can try creating a new block over the 
    * genesis block (block height 2) if the block chain height
    * is {@code <= CUT_OFF_AGE + 1}. As soon as {@code height >
    * CUT_OFF_AGE + 1}, you cannot create a new block at height
    * 2.
    * @return true if block is successfully added
    */
  public boolean addBlock(Block block) {
    // IMPLEMENT THIS
  }

  /** Add a transaction to the transaction pool */
  public void addTransaction(Transaction tx) {
    // IMPLEMENT THIS
  }
}
```

The **BlockChain** class is responsible for maintaining a block chain. Since the entire block chain could be huge in size, you should only keep around the most recent blocks. The exact number to store is up to your design, as long as you’re able to implement all the API functions.

Since there can be (multiple) forks, blocks form a tree rather than a list. Your design should take this into account. You have to maintain a UTXO pool corresponding to every block on top of which a new block might be created.

