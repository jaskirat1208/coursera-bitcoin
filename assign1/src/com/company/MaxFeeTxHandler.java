package com.company;

import java.util.*;

public class MaxFeeTxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    private UTXOPool pool;
    public MaxFeeTxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        pool = utxoPool;
    }
    private double getProfit(Transaction tx){
        double prev_sum=0,curr_sum=0;
        for (Transaction.Input in : tx.getInputs()) {
            UTXO key = new UTXO(in.prevTxHash,in.outputIndex);
            if (!pool.contains(key) || !(isValidTx(tx))) {
                continue;
            }
            Transaction.Output out = pool.getTxOutput(key);
            prev_sum += out.value;
        }
        for (Transaction.Output out : tx.getOutputs()) {
            curr_sum += out.value;
        }
        System.out.println(prev_sum - curr_sum);
        return prev_sum - curr_sum;
    }
    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,                        done
     * (3) no UTXO is claimed multiple times by {@code tx},                             done
     * (4) all of {@code tx}s output values are non-negative, and                       done
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
//        ArrayList<Transaction.Output> prev_out_list = tx.;
//        ArrayList<Transaction.Output> curr_out_list = tx.getOutputs();
        double prev_sum=0, curr_sum = 0;
        UTXOPool unique_pool = new UTXOPool();
        for (int i=0;i<tx.numInputs();i++){
            Transaction.Input in = tx.getInput(i);
            //check the signatures
            UTXO key = new UTXO(in.prevTxHash,in.outputIndex);
            if (!pool.contains(key)) {
                return false;
            }
            Transaction.Output out = pool.getTxOutput(key);
            boolean is_sig_valid = Crypto.verifySignature(out.address,tx.getRawDataToSign(i),in.signature);
            if (is_sig_valid){
//                System.out.println("Signature at index "+ i + " is valid");
            }
            else{
                return false;
            }
            
//          Calculation of previous output value sum
            if (out.value<0){
//                System.out.println("Negative values");
                return false;
            }
            prev_sum += out.value;
//            Check for uniqueness
            if (unique_pool.contains(key)){
//                System.out.println("Uniqueness Violated");
                return false;
            }
//          Enqueue it inside the uniquepool so as to check the uniqueness
            unique_pool.addUTXO(key,out);

        }
        for (int i=0;i<tx.numOutputs();i++){
            if (tx.getOutput(i).value<0)    return false;
            curr_sum+= tx.getOutput(i).value;
        }
        if (prev_sum < curr_sum) {
            return false;
        }
//        System.out.println("Current Sum: "+ curr_sum);
//        System.out.println("Previous sum: "+prev_sum);
        return true;

    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        // ArrayList<Transaction> lst = new ArrayList<Transaction>();
        ArrayList<Transaction> lst = new ArrayList<Transaction>();
        ArrayList<Transaction> tmp_lst = new ArrayList<Transaction>();
        double profit =0;
        Collections.sort(possibleTxs, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction transaction, Transaction t1) {
                return (int)Math.signum(getProfit(transaction) - getProfit(t1));
            }

        });
        for (int i=0;i<possibleTxs.length;i++){
//            If transaction is valid, add the outputs to the pool, removing its inputs.
            if(isValidTx(possibleTxs[i])){
                System.out.println("A possible transaction found");
                tmp_lst.add(possibleTxs[i]);
                double profit_get = getProfit(possibleTxs[i]); 
                if ( profit_get >= profit ) {
                    profit = profit_get;
                    lst = new ArrayList<Transaction>(tmp_lst);
                    for (Transaction tx : lst) {
                        for (Transaction.Input in : tx.getInputs()) {
                            UTXO to_rm = new UTXO(in.prevTxHash,in.outputIndex);
                            pool.removeUTXO(to_rm);
                        }
                        int index =0;
                        for (Transaction.Output out : tx.getOutputs()) {
                               UTXO to_add = new UTXO(tx.getHash(),index);
                               pool.addUTXO(to_add,out);
                               index++;
                        }
                    }   
                }
            }
            tmp_lst.clear();
        }
                
        Transaction[] lst_final = lst.toArray(new Transaction[lst.size()]);
        return lst_final;
    }

}
