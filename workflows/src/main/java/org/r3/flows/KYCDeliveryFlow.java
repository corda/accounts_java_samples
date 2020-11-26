package org.r3.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.flows.AccountInfoByName;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FinalityFlow;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import org.r3.contracts.AssetForAccountContract;
import org.r3.contracts.AssetForAccountState;

import java.security.PublicKey;
import java.util.Collections;

@StartableByRPC
public class KYCDeliveryFlow extends FlowLogic<SignedTransaction> {

    // Define your properties and parties here
    private final String acctNameSource;
    private final String acctNameTarget;
    private final StateAndRef<AssetForAccountState> asset;
//    private final String name;
//    private final long value;


    public KYCDeliveryFlow(String acctNameSource, String acctNameTarget, StateAndRef<AssetForAccountState> asset) {
        this.acctNameSource = acctNameSource;
        this.acctNameTarget = acctNameTarget;
        this.asset=asset;

//        this.name = name;
//        this.value = value;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // NOTE NOTE NOTE
        // This is a node level self-issuance so using account name as identifier is sufficient
        // HOWEVER at the network level you can not enforce uniqueness on the acctName alone.
        // user either i) tuple of acctName and host, OR ii) UUID

        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
//

        // Source
        AccountInfo issuerOwner = subFlow(new AccountInfoByName(acctNameSource)).get(0).getState().getData();
        PublicKey issuerOwnerKey = getServiceHub().getKeyManagementService().freshKey(issuerOwner.getIdentifier().getId());
        AnonymousParty issuerOwnerAnonParty = new AnonymousParty(issuerOwnerKey);

        AccountInfo targetOwner = subFlow(new AccountInfoByName(acctNameTarget)).get(0).getState().getData();
        PublicKey targetOwnerKey = getServiceHub().getKeyManagementService().freshKey(issuerOwner.getIdentifier().getId());
        AnonymousParty targetOwnerAnonParty = new AnonymousParty(targetOwnerKey);


        AssetForAccountState outputAsset = new AssetForAccountState(issuerOwnerAnonParty, targetOwnerAnonParty,
                asset.getState().getData().getName(), asset.getState().getData().getValue());


        TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addInputState(asset)
                .addOutputState(outputAsset, AssetForAccountContract.ID)
                .addCommand(new AssetForAccountContract.Commands.Transfer(), issuerOwnerKey);

        txBuilder.verify(getServiceHub());

        SignedTransaction fullySignedTransaction = getServiceHub().signInitialTransaction(txBuilder, issuerOwnerKey);

        //return fullySignedTransaction;
        return subFlow(new FinalityFlow(fullySignedTransaction, Collections.emptyList()));
    }
}
