// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.
import java.util.ArrayList;
import java.sql.Timestamp;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    private TransactionPool _transPool = new TransactionPool();
    private ArrayList<BlockNode> _blockchain = new ArrayList<>();
    private BlockNode _maxHeightNode;

    public void updateMaxHeightNode() {
        BlockNode currentMaxHeightNode = _maxHeightNode;
        for (BlockNode b : _blockchain) {
            if (b._height > currentMaxHeightNode._height) {
                currentMaxHeightNode = b;
            } else if (b._height == currentMaxHeightNode._height) {
                if (currentMaxHeightNode._createAt.after(b._createAt)) {
                    currentMaxHeightNode = b;
                }
            }
        }
        _maxHeightNode = currentMaxHeightNode;
    }

    public BlockNode getParentNode(byte[] blockHash) {
        ByteArrayWrapper b1 = new ByteArrayWrapper(blockHash);
        for (BlockNode b : _blockchain) {
            ByteArrayWrapper b2 = new ByteArrayWrapper(b._block.getHash());
            if (b1.equals(b2)) {
                return b;
            }
        }
        return null;
    }

    public class BlockNode {
        private Block _block;
        private int _height = 0;
        private UTXOPool _utxoPool = new UTXOPool();
        private TransactionPool _transPool = new TransactionPool();
        private Timestamp _createAt;

        public BlockNode(Block block, int height, UTXOPool utxoPool, TransactionPool transPool) {
            this._block = block;
            this._height = height;
            this._utxoPool = utxoPool;
            this._transPool = transPool;
            this._createAt = new Timestamp(System.currentTimeMillis());
        }

        public UTXOPool getUTXOPool() {
            return this._utxoPool;
        }

        public TransactionPool getTransactionPool() {
            return this._transPool;
        }
    }

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        UTXOPool utxoPool = new UTXOPool();
        TransactionPool transPool = new TransactionPool();
        for (int i = 0; i < genesisBlock.getCoinbase().numOutputs(); i++) {
            utxoPool.addUTXO(new UTXO(genesisBlock.getCoinbase().getHash(),i),genesisBlock.getCoinbase().getOutput(i));
        }
        transPool.addTransaction(genesisBlock.getCoinbase());
        for (Transaction t : genesisBlock.getTransactions()) {
            if (t != null) {
                for (int i=0;i<t.numOutputs();i++) {
                    Transaction.Output output = t.getOutput(i);
                    UTXO utxo = new UTXO(t.getHash(),i);
                    utxoPool.addUTXO(utxo,output);
                }
                transPool.addTransaction(t);
            }
        }
        BlockNode b = new BlockNode(genesisBlock, 1, utxoPool, transPool);
        _maxHeightNode = b;
        _blockchain.add(b);
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return _maxHeightNode._block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return _maxHeightNode._utxoPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return _transPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        //check block is genesisBlock?
        if (block.getPrevBlockHash() == null) {
            return false;
        }
        //check parent Hash
        BlockNode parentNode = getParentNode(block.getPrevBlockHash());
        if(parentNode == null) {
            return false;
        }
        //compare height
        int blockHeight = parentNode._height+1;
        if (blockHeight <= _maxHeightNode._height - CUT_OFF_AGE) {
            return false;
        }

        //check all transactions in block are valid?
        UTXOPool utxoPool = new UTXOPool(parentNode.getUTXOPool());
        TransactionPool transPool = new TransactionPool(parentNode.getTransactionPool());
        for (Transaction t : block.getTransactions()) {
            TxHandler txHandler = new TxHandler(utxoPool);
            if (!txHandler.isValidTx(t)) {
                return false;
            }
            //remove used utxo
            for (Transaction.Input input : t.getInputs()) {
                int outputIndex = input.outputIndex;
                byte[] prevTxHash = input.prevTxHash;
                UTXO utxo = new UTXO(prevTxHash, outputIndex);
                utxoPool.removeUTXO(utxo);
            }
            //add new utxo
            byte[] hash = t.getHash();
            for (int i=0;i<t.numOutputs();i++) {
                UTXO utxo = new UTXO(hash, i);
                utxoPool.addUTXO(utxo, t.getOutput(i));
            }
        }

        //update utxo transaction coinbase
        for (int i = 0; i < block.getCoinbase().numOutputs(); i++) {
            utxoPool.addUTXO(new UTXO(block.getCoinbase().getHash(),i),block.getCoinbase().getOutput(i));
        }

        //remove trans pool
        for (Transaction t : block.getTransactions()) {
            transPool.removeTransaction(t.getHash());
        }

        //add new block
        BlockNode b = new BlockNode(block,blockHeight,utxoPool,transPool);
        boolean addNewBlock = _blockchain.add(b);
        if (addNewBlock) {
            updateMaxHeightNode();
            _transPool = transPool;
        }
        return addNewBlock;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        _transPool.addTransaction(tx);
    }
}