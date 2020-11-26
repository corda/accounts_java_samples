package org.r3.tests;

import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.flows.AccountInfoByName;
import com.r3.corda.lib.accounts.workflows.flows.CordappVersionDetector;
import com.r3.corda.lib.accounts.workflows.flows.CreateAccount;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.r3.contracts.AssetForAccountState;
import org.r3.flows.*;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class FlowTests {
    private MockNetwork network;
    private StartedMockNode nodeA;
    private StartedMockNode nodeB;

    @Before
    public void setup() {
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
        network.runNetwork();
        nodeA.startFlow(new CreateAccount("BobAcct"));
        nodeB.startFlow(new CreateAccount("AliceAcct"));
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }


    @Test
    public void allocation() throws Exception {
        nodeA.startFlow(new CreateAccount("corporateA"));
        //nodeA.startFlow(new CreateAccount("corporateB"));
        KYCAllocationFlow flow = new KYCAllocationFlow ("corporateA", "kycdata", 10);
        CordaFuture<SignedTransaction> future = nodeA.startFlow(flow);
        network.runNetwork();
        SignedTransaction issueTx = future.get();
        System.out.println(issueTx);
        assertNotNull(issueTx);
    }

    @Test
    public void delivery() throws Exception {
        nodeA.startFlow(new CreateAccount("corporateA"));
        nodeA.startFlow(new CreateAccount("corporateB"));

        KYCAllocationFlow flow = new KYCAllocationFlow ("corporateA", "kycdata", 10);
        CordaFuture<SignedTransaction> future = nodeA.startFlow(flow);
        network.runNetwork();
        SignedTransaction issueTx = future.get();
        assertNotNull(issueTx);

        StateAndRef<AssetForAccountState> assetOut = issueTx.getTx().outRef(0);
        assertNotNull(assetOut);
        network.runNetwork();
        KYCDeliveryFlow flowDel = new KYCDeliveryFlow ("corporateA", "corporateB",assetOut);
        CordaFuture<SignedTransaction> futureDel = nodeA.startFlow(flowDel);
        network.runNetwork();
        SignedTransaction delTx = futureDel.get();
        assertNotNull(delTx);



    }

    // Node account to account tests
    @Test
    public void nodeToNodeAccountTx() throws Exception {
        AssetForAccountStateSelfIssueFlow flow = new AssetForAccountStateSelfIssueFlow("BobAcct", "diamond", 10);
        CordaFuture<SignedTransaction> future = nodeA.startFlow(flow);
        network.runNetwork();
        SignedTransaction issueTx = future.get();
        System.out.println(issueTx);

        // get AccountInfo for AliceAcct
        Object o = new AccountInfoByName("AliceAcct");
        CordaFuture<List<? extends StateAndRef<? extends AccountInfo>>> future3 = nodeB.startFlow(new AccountInfoByName("AliceAcct"));
        network.runNetwork();

        StateAndRef<AccountInfo> aliceAcctInfo = (StateAndRef<AccountInfo>) future3.get().get(0);

        // share Alice with nodeA
        nodeB.startFlow(new ShareAccountToParty("AliceAcct", nodeA.getInfo().getLegalIdentities().get(0)));
        //nodeB.startFlow(new ShareAccountInfo(aliceAcctInfo, ImmutableList.of(nodeA.getInfo().getLegalIdentities().get(0))));
        network.runNetwork();

        NodeAccountToNodeAccountFlows.NodeAccountToNodeAccountInitiatorFlow flow2 = new NodeAccountToNodeAccountFlows.NodeAccountToNodeAccountInitiatorFlow(
                "BobAcct", "AliceAcct", issueTx.getId().toString(), 0
        );
        CordaFuture<SignedTransaction> future2 = nodeA.startFlow(flow2);
        network.runNetwork();
        SignedTransaction moveTx = future2.get();
        System.out.println(moveTx);


        Assert.assertEquals(1, moveTx.getTx().getOutputStates().size());
    }

    /**
     * Get the version of the CorDapp, verify it complies with
     * what we expect
     */
//    @Test
//    public void getCorDappVersion() throws Exception {
//        CordappVersionDetector flow = new CordappVersionDetector();
//        CordaFuture<String> future = nodeA.startFlow(flow);
//        network.runNetwork();
//        String result = future.get();
//    }

}