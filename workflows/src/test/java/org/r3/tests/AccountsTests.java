package org.r3.tests;

import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.flows.CreateAccount;
import com.r3.corda.lib.accounts.workflows.flows.ShareAccountInfo;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.StateRef;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.r3.flows.KYCAllocationFlow;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AccountsTests {
    private MockNetwork network;
    private StartedMockNode nodeA;
    private StartedMockNode nodeB;
    private StartedMockNode nodeC;
    private StateAndRef<AccountInfo> AccAlice;
    private StateAndRef<AccountInfo> AccBob;
    private StateAndRef<AccountInfo> AccJoe;

    @Before
    public void setup() throws ExecutionException, InterruptedException {
        network = new MockNetwork(
                new MockNetworkParameters(
                        ImmutableList.of(
                                TestCordapp.findCordapp("org.r3.contracts"),
                                TestCordapp.findCordapp("org.r3.flows"),
                                TestCordapp.findCordapp("com.r3.corda.lib.ci"),
                                TestCordapp.findCordapp("com.r3.corda.lib.accounts.contracts"),
                                TestCordapp.findCordapp("com.r3.corda.lib.accounts.workflows")
                        )
                ));
        nodeA = network.createPartyNode(null);
        nodeB = network.createPartyNode(null);
        nodeC = network.createPartyNode(null);
        AccAlice = (StateAndRef<AccountInfo>) nodeA.startFlow(new CreateAccount("Alice")).get(); // start CreateAccount name: 'Alice'
        AccBob = (StateAndRef<AccountInfo>) nodeA.startFlow(new CreateAccount("Bob")).get(); // start CreateAccount name: 'Bob'
        AccJoe = (StateAndRef<AccountInfo>) nodeC.startFlow(new CreateAccount("Joe")).get(); // start CreateAccount name: 'Joe'
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void assetIssue() throws ExecutionException, InterruptedException {
        nodeA.getServices().getKeyManagementService().getKeys().forEach( key-> {
            System.out.println(key.getEncoded().toString());
        });
        CordaFuture<SignedTransaction> future = nodeA.startFlow(new KYCAllocationFlow("Alice", "USD", 100));
        // start KYCAllocationFlow acctName: 'Alice', name: 'USD', value: 100
        network.runNetwork();
        future.get();
        nodeA.startFlow(new ShareAccountInfo(AccAlice, nodeC.getInfo().getLegalIdentities())); // start ShareAccountToParty acctName: 'Alice', shareTo: 'PartyC'
        network.runNetwork();
    }

}
