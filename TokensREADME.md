<p align="center">
  <img src="https://camo.githubusercontent.com/a7b7d659d6e01a9e49ff2d9919f7a66d84aac66e/68747470733a2f2f7777772e636f7264612e6e65742f77702d636f6e74656e742f75706c6f6164732f323031362f31312f66673030355f636f7264615f622e706e67" alt="Corda" width="500">
  <p></p>
 
</p>


Exercises below are to demonstrate the functionality of the Corda Accounts package



# Pre-Requisites

See https://docs.corda.net/getting-set-up.html.

# Materials

All slide decks and Corda Design Language (CDL) diagrams can be found in the `resources` folder

# Usage

## Running the nodes

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.

## Interacting with the nodes



### Shell Commands for executing DVP with TokenSDK

When started via the command line, each node will display an interactive shell:

    Welcome to the Corda interactive shell.
    Useful commands include 'help' to see what is available, and 'bye' to shut down the node.
    
    Tue July 09 11:58:13 GMT 2019>>>

You can use this shell to interact with your node.

First go to the shell of PartyA and issue some USD to Party C. We will need the fiat currency to exchange it for the asset token. 

    start FiatCurrencyIssueFlow currency: USD, amount: 100000000, recipient: PartyC

We can now go to the shell of PartyC and check the amount of USD issued. Since fiat currency is a fungible token we can query the vault for FungibleToken states.

    run vaultQuery contractStateType: com.r3.corda.lib.tokens.contracts.states.FungibleToken
    
Once we have the USD issued to PartyC, we can Create and Issue the AssetToken to PartyB. Goto PartyA's shell to create and issue the asset token.
    
    start AssetTokenCreateAndIssueFlow owner: PartyB, name: "awesome asset", value: 10000 USD
    
We can now check the issued asset token in PartyB's vault. Since we issued it as a non-fungible token we can query the vault for non-fungible tokens.
    
    run vaultQuery contractStateType: com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
    
Note that asset token is an evolveable token which is a linear state, thus we can check PartyB's vault to view the evolveable token

    run vaultQuery contractStateType: com.bootcamp.day2.states.AssetTokenState
    
Note the linearId of the asset token from the previous step, we will need it to perform our DvP operation. Goto PartyB's shell to initiate the token sale.
    
    start AssetTokenSaleInitiatorFlow assetStateId: <XXXX-XXXX-XXXX-XXXXX>, buyer: PartyC
    
We could now verify that the non-fungible token has been transferred to PartyC and some 100,000 USD from PartyC's vault has been transferred to PartyB. Run the below commands in PartyB and PartyC's shell to verify the same
    
    // Run on PartyB's shell
    run vaultQuery contractStateType: com.r3.corda.lib.tokens.contracts.states.FungibleToken
    // Run on PartyC's shell
    run vaultQuery contractStateType: com.r3.corda.lib.tokens.contracts.states.NonFungibleToken

---