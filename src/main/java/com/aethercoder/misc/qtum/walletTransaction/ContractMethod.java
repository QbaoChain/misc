package com.aethercoder.misc.qtum.walletTransaction;

import java.util.List;


public class ContractMethod{

    public boolean constant;

    public List<ContractMethodParameter> inputParams;

    public String name;

    public List<ContractMethodParameter> outputParams;

    public boolean payable;

    public String type;

    public ContractMethod(){

    }

    public ContractMethod(boolean constant, String type, List<ContractMethodParameter> inputParams, String name, List<ContractMethodParameter> outputParams) {
        this.constant = constant;
        this.type = type;
        this.inputParams = inputParams;
        this.name = name;
        this.outputParams = outputParams;
    }

    public boolean isConstant() {
        return constant;
    }

    public List<ContractMethodParameter> getInputParams() {
        return inputParams;
    }

    public String getName() {
        return name;
    }

    public List<ContractMethodParameter> getOutputParams() {
        return outputParams;
    }

    public boolean isPayable() {
        return payable;
    }

    public String getType() {
        return type;
    }
}
