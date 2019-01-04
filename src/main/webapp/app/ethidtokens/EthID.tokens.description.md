Ethereum Identity (EthID) tokens by Cryptonomica 
================================================

Last revision: 2018-12-26 
Published on: https://github.com/Cryptonomica/cryptonomica.github.io/blob/master/app/ethidtokens/EthID.tokens.description.md 
Google Doc (draft): https://docs.google.com/document/d/15dr27enz30M0qjsWms5BMMiOZ-S84aQ_Zwm8NfqEGZM/ 

## Identity verification by Cryptonomica.
[Cryptonomica](https://cryptonomica.net) made and is developing online and offline digital identity system. A detailed description of the Cryptonomica’s activities is set out in its ['White Paper'](http://bit.ly/Cryptonomica-White-Paper)

Basically, user identity is connected to cryptographic key in [OpenPGP](https://en.wikipedia.org/wiki/Pretty_Good_Privacy) standard. 
And it can also be connected to blockchain address. 
So we can use OpenPGP as a universal and cross-chain digital identity. 

Cryptonomica developed system for identity verification for [Etherium](https://www.ethereum.org) blockchain. This system is simpler and more friendly to developers of smart contracts than all other existing systems.

Owner of the eth address is verified using his/her previously verified OpenPGP key : https://cryptonomica.net/#!/verifyEthAddress/  

User data are stored in smart contract: https://etherscan.io/address/0x846942953c3b2A898F10DF1e32763A823bf6b27f#readContract  

This smart contract has simple and developer friendly interface, and can be used (for free) as an extension for other smart contracts. 

### Use cases.
Cryptonomica Ethereum address verification implemented on [SONM](https://sonm.com), see: [How to pass KYC with Cryptonomica](https://docs.sonm.com/how-to/how-to-pass-kyc-by-cryptonomica) 

## Token definition.
EthID token is a cryptocurrency backed in ether in smart contract on the Ethereum blockchain,  representing a share in the revenue from Ethereum address verification system developed by Cryptonomica.

Tokens conform to the [ERC-20](https://github.com/ethereum/EIPs/blob/master/EIPS/eip-20.md) standard. 

Smart contract code: https://github.com/Cryptonomica/cryptonomica.github.io/blob/master/app/ethidtokens/EthID.sol 

Smart contract address: 
https://etherscan.io/address/0xe5374b3840f0782608f86e270ae389b8ee2024c4 

Verified smart contract code: 
https://etherscan.io/address/0xe5374b3840f0782608f86e270ae389b8ee2024c4#code 

## Token distribution. 
There will be no crowdfunding and no public offering.
Part of tokens will be sold on private placement, part will be held by the company and will be used in settlements with suppliers, partners, employees, shareholders, advisers.

Cryptonomica will provide a listing of tokens on legally operating exchanges and marketplaces. 

## Token economic model.
A finite limited number of tokens will be issued in the smart contract on the Ethereum blockchain. It will be 100M tokens.

Cryptonomica accumulates all revenue from Ethereum identity verification smart contract [0x846942953c3b2A898F10DF1e32763A823bf6b27f](https://etherscan.io/address/0x846942953c3b2A898F10DF1e32763A823bf6b27f#readContract)
All payments made to that smart contract will be transferred to smart contract with tokens. 

Any token holder can at any time burn his/her tokens, and immediately receive a share of the current sumo of EHT in smart contract, the same as the share of the burned tokens in the current total supply of tokens. Thus, we have a smart contract in which more and more funds accumulate, and number of tokens become less and less. 

Of course, token holder can instead of burning a token, sell it, and probably get better revenue. 
EthID token is already listed on [stex.com](https://stex.com) , a licensed Estonian cryptocurrency exchange that works both with fiat and cryptocurrencies. EthID/ETH :  https://app.stex.com/en/trade/pair/ETH/EthID/30 
EthID token derivative ([Asset CRYPTONOMICA](https://wallet.bitshares.org/#/asset/CRYPTONOMICA)) is listed and traded on [bitshares](https://wallet.bitshares.org/#/asset/CRYPTONOMICA) via [deex.exchange](https://deex.exchange/asset/CRYPTONOMICA/) 
Thus instead of burning a token a token holder can sell it on stex.com, deex.exchange or other cryptocurrency exchanges. 


In such way, we solve the fundamental problem faced by investors buying tokens on the Ethereum: the price growth of these tokens often is is less than the growth of the ETH. In our case, the tokens acquire a minimum value equal to the amount of ether on which they can be immediately exchanged, and this amount cannot become smaller over time. 

## Team.
### Cryptonomica core team: 
* Viktor Ageyev, software engineer and lawyer, CEO of Cryptonomica, https://github.com/ageyev  
* Max Baryshnikov, lawyer, CFO of Cryptonomica, https://github.com/MaxBaryshnikov  

### Advisers:
* Vadym Kurylovych, founder of [stex.com](https://www.stex.com), a licenced Estonian cryptocurrency exchange

## Differences from other projects and approaches.
### Is it a 'self-sovereign identity'? 
It depends how do you define self-sovereign identity. In our system only user controls his/her private keys 
and no other person can make a signature or transaction in the name of this user or get his/her private key. 

But in our system there is a centralized repository of user data (such as user documents, video etc.) controlled by court of arbitration, 
and these data can be used in legal dispute. So our company acts like an 'identity escrow' for electronic and smart contracts. 

### Is it acceptable to store user data on blockchain in clear text? 
We store a minimal amount of data on the blockchain. Actually only information that particular address is owned by particular person. 

Storing information in this form allows its use by other smart contracts directly on the blockchain without recourse to other software.

### What about storing personal data on blockchain and [GDPR](https://gdpr-info.eu)? 
In this model, we act as a service through which the user can place data on the blockchain. Like email service used to send a message from one user to another.

But after the data is placed in the blockchain, we are not more data processor, nor data controller. Like email service does not control the message received by the addressee. 

We do not control the blockchain. In accordance with the [GDPR](https://gdpr-info.eu), the user may have the right to delete the data, but in this case user has to contact the miners, all of them. 

## The market and competitors 
For the list of competing projects and related resources see [Blockchain and Identity](https://github.com/peacekeeper/blockchain-identity)

See also [Awesome Decentralized Identity](https://github.com/infominer33/awesome-decentralized-id)

For the market history see [Evolution of Online Identity](https://wiki.p2pfoundation.net/Evolution_of_Online_Identity), 
[Online identity](http://en.wikipedia.org/wiki/Online_identity).

## Where to get more info and support? 
Email: support@cryptonomica.net 

Telegram: https://t.me/cryptonomicanet 

Facebook: https://www.facebook.com/groups/cryptonomica/ 

Reddit: https://www.reddit.com/r/cryptonomica/ 

Wiki on github: https://github.com/Cryptonomica/cryptonomica/wiki 

