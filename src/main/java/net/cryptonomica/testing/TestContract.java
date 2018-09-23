package net.cryptonomica.testing;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import rx.Observable;
import rx.functions.Func1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.5.0.
 */
public class TestContract extends Contract {
    private static final String BINARY = "608060405234801561001057600080fd5b5060008054600160a060020a031916331790556102d5806100326000396000f30060806040526004361061006c5763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416637dc9392981146100715780638da5cb5b146100fb578063cb297fc514610139578063cb398f2f14610162578063ebba140014610189575b600080fd5b34801561007d57600080fd5b506100866101a1565b6040805160208082528351818301528351919283929083019185019080838360005b838110156100c05781810151838201526020016100a8565b50505050905090810190601f1680156100ed5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561010757600080fd5b5061011061022c565b6040805173ffffffffffffffffffffffffffffffffffffffff9092168252519081900360200190f35b34801561014557600080fd5b5061014e610248565b604080519115158252519081900360200190f35b34801561016e57600080fd5b50610177610251565b60408051918252519081900360200190f35b34801561019557600080fd5b5061014e600435610257565b6002805460408051602060018416156101000260001901909316849004601f810184900484028201840190925281815292918301828280156102245780601f106101f957610100808354040283529160200191610224565b820191906000526020600020905b81548152906001019060200180831161020757829003601f168201915b505050505081565b60005473ffffffffffffffffffffffffffffffffffffffff1681565b60035460ff1681565b60015481565b6001805490829055604080518281526020810184905281516000939233927fcf19d10be998a11a4f3dffa95dd7fd6f55bf303a251f296c3f2e3278302c90b4929081900390910190a2506001929150505600a165627a7a723058209966b3a83cd4ddc19f98c3e9c50b0a050ef01237a5d8a0b01731fc6a772bc99d0029";

    public static final String FUNC_STRINGVALUE = "stringValue";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_BOOLEANVALUE = "booleanValue";

    public static final String FUNC_INTEGERVALUE = "integerValue";

    public static final String FUNC_SETINTEGERVALUE = "setIntegerValue";

    public static final Event INTEGERVALUECHANGED_EVENT = new Event("IntegerValueChanged", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>(true) {}));
    ;

    protected TestContract(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected TestContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public RemoteCall<String> stringValue() {
        final Function function = new Function(FUNC_STRINGVALUE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<Boolean> booleanValue() {
        final Function function = new Function(FUNC_BOOLEANVALUE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<BigInteger> integerValue() {
        final Function function = new Function(FUNC_INTEGERVALUE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> setIntegerValue(BigInteger _integerValue) {
        final Function function = new Function(
                FUNC_SETINTEGERVALUE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_integerValue)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public static RemoteCall<TestContract> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(TestContract.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<TestContract> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(TestContract.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public List<IntegerValueChangedEventResponse> getIntegerValueChangedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(INTEGERVALUECHANGED_EVENT, transactionReceipt);
        ArrayList<IntegerValueChangedEventResponse> responses = new ArrayList<IntegerValueChangedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            IntegerValueChangedEventResponse typedResponse = new IntegerValueChangedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.by = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.from = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.to = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<IntegerValueChangedEventResponse> integerValueChangedEventObservable(EthFilter filter) {
        return web3j.ethLogObservable(filter).map(new Func1<Log, IntegerValueChangedEventResponse>() {
            @Override
            public IntegerValueChangedEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(INTEGERVALUECHANGED_EVENT, log);
                IntegerValueChangedEventResponse typedResponse = new IntegerValueChangedEventResponse();
                typedResponse.log = log;
                typedResponse.by = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.from = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.to = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Observable<IntegerValueChangedEventResponse> integerValueChangedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(INTEGERVALUECHANGED_EVENT));
        return integerValueChangedEventObservable(filter);
    }

    public static TestContract load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new TestContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static TestContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TestContract(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class IntegerValueChangedEventResponse {
        public Log log;

        public String by;

        public BigInteger from;

        public BigInteger to;
    }
}
