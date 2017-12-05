package com.jaskirat;

import java.util.*;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory
// as it would cause a memory overflow.
public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    private class SuperBlock{
        SuperBlock previousBlock;
        byte[] blk_hash;
        Block block;
        UTXOPool utxoPool;
        int height;
        int timestamp;
        private SuperBlock(SuperBlock sb,byte[] blk_hash, Block blk,int height, int timestamp){
            this.previousBlock = sb;
            this.blk_hash = Arrays.copyOf(blk_hash,blk_hash.length);
            this.block = blk;
            this.utxoPool = new UTXOPool();
            this.height = height;
            this.timestamp = timestamp;
            txpool = new TransactionPool();
        }
    }
    TransactionPool txpool;
    SuperBlock sb;
    int timestamp;
    private HashMap<byte[],SuperBlock> map;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        map = new HashMap<byte[], SuperBlock>();
        timestamp = 1;
        timestamp++;
        sb = new SuperBlock(null, genesisBlock.getHash(), genesisBlock,1,1);
        Transaction coinbase = genesisBlock.getCoinbase();
        for (int i = 0; i < coinbase.numOutputs(); i++) {
            System.out.println("OUTPUT "+ i);
            coinbase.getOutput(i);
            UTXO key = new UTXO(genesisBlock.getHash(),i);
//            Add to UTXOPool of sb
            sb.utxoPool.addUTXO(key,coinbase.getOutput(i));
        }
        System.out.println(sb.utxoPool.getAllUTXO().size()+ "= Pool size ");
        map.put(genesisBlock.getHash(),sb);
    }
    public Block getBlockParent(Block blk){
        if (!map.containsKey(blk.getHash())) return null;
        SuperBlock sb = map.get(blk.getHash());
        if (sb.previousBlock==null) return null;
        return sb.previousBlock.block;
    }
    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        return sb.block;
    }
    public int getMaxHeight(){
        return sb.height;
    }
    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return new UTXOPool(sb.utxoPool);
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return txpool;
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
        // IMPLEMENT THIS
        if(!map.containsKey(block.getPrevBlockHash())){
            return false;
        }
        SuperBlock prev_blk = map.get(block.getPrevBlockHash());
        /*------------------------------------------------*/
        System.out.println("parent UTXO: "  + prev_blk.utxoPool.getAllUTXO().size());
        Transaction[] arr = block.getTransactions().toArray(new Transaction[0]);
        for (int i=0;i<arr.length;i++){
            System.out.println("Transaction "+ i+ ": "+ arr[i].numInputs());
        }
        TxHandler txHandler = new TxHandler(new UTXOPool(prev_blk.utxoPool));
        Transaction[] validarr = txHandler.handleTxs(arr);
        if (validarr.length!=arr.length){
            System.out.println("INVALID TRANSACTIONS: "+ validarr.length + " "+arr.length);
            return false;
        }
        /*------------------------------------------------*/
        SuperBlock temp;
        if (prev_blk!=null)
            temp =new SuperBlock(prev_blk,block.getHash(),block,prev_blk.height+1,timestamp);
        else{
            System.out.println("Null previous block" + block.getHash());
            temp = new SuperBlock(null,block.getHash(),block,1,timestamp);
        }
        if (temp.height <= sb.height- CUT_OFF_AGE){
            System.out.println("HEIGHT Unbalanced");
            return false;
        }
        map.put(block.getHash(),temp);
        if (temp.height>sb.height)  sb=temp;
        System.out.println(timestamp);
        timestamp++;
        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        txpool.addTransaction(tx);
    }
    public void print(){
//        Set<SuperBlock> set = new HashSet<>();
        Collection<SuperBlock> set = map.values();
        for (SuperBlock s :
                set) {
            System.out.println("Block Hash: "+s.blk_hash+ ", height: "+ s.height+ ", parentHash: "+ s.block.getPrevBlockHash());
        }
    }
}