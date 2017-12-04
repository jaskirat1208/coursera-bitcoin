package com.jaskirat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    boolean[] followees,blacklist;
    double p_graph,p_malicious,p_txDist,numRounds;
    Set<Transaction> pending_tx = new HashSet<>();;
    Set<Candidate> list = new HashSet<Candidate>();
    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDist = p_txDistribution;
        this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        this.followees = followees;
        blacklist=new boolean[followees.length];
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        this.pending_tx = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        Set<Transaction> tx_isfollowing= new HashSet<>();
        for (Transaction tx : this.pending_tx) {
            tx_isfollowing.add(tx);
        }
        pending_tx.clear();
        return  tx_isfollowing;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        list = candidates;
        ArrayList<Integer> received = new ArrayList<>();
        for (Candidate cd: candidates){
            received.add(cd.sender);
        }
        for (int i=0;i<followees.length;i++){
            //If I am not receiving any transaction from a followee, it is malicious.

            if (followees[i]==true && !(received.contains(i))/*I have not received any transaction*/)
                blacklist[i] = true;
        }
        for (Candidate cd : candidates){
//            System.out.println(cd.sender);
            if (!blacklist[cd.sender]){
                pending_tx.add(cd.tx);
            }
        }
        // IMPLEMENT THIS
    }
}
