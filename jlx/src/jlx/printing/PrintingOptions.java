package jlx.printing;

import java.util.ArrayList;
import java.util.List;

public class PrintingOptions {
	public boolean USE_PREPROCESSED_ASAL;
	public boolean COMBINE_SMS_ES;
	public int EVENT_QUEUE_BOUND;
	public boolean ADD_inState_SELFLOOPS;
	public boolean ADD_ALL_inState_SELFLOOPS;
	public List<String> ALLOWED_STATES_inState_SELFLOOPS;
	public boolean ADD_inEventPool_SELFLOOPS;
	public boolean ADD_ALL_inEventPool_SELFLOOPS;
	public List<String> ALLOWED_EVENTS_inEventPool_SELFLOOPS;
	public boolean ADD_varVal_SELFLOOPS;
	public boolean ADD_ALL_varVal_SELFLOOPS;
	public List<String> ALLOWED_VARS_varVal_SELFLOOPS;
	public boolean USE_SYNCHRONOUS_PORTS;
	public boolean RESET_VARIABLES_INITIAL;
//	public boolean PRINT_SYSIM_TEST_ADAPTER;
	public boolean USE_PULSE_PACKS;
	public boolean USE_ENV_RESTRICTION;
	public boolean ADD_ABSTRACT_inEventPool_SELFLOOPS;
	
	/**
	 * Experimental. Include process parameters that can toggle a process' ability
	 * to do certain things on/off. Used for a sequential execution model.
	 */
	public boolean SEQ_EXECUTION_TOGGLES;
	
	/**
	 * Experimental. Make the model sequential by forcing synchronization with a
	 * phases process.
	 */
	public boolean SEQ_EXECUTION;
	
	public PrintingOptions() {
		this.USE_PREPROCESSED_ASAL = false;
		this.COMBINE_SMS_ES = true;
		this.EVENT_QUEUE_BOUND = 1;
		this.ADD_inState_SELFLOOPS = false;
		this.ADD_ALL_inState_SELFLOOPS = false;
		this.ADD_inEventPool_SELFLOOPS = false;
		this.ADD_ALL_inEventPool_SELFLOOPS = false;
		this.RESET_VARIABLES_INITIAL = false;
		this.ADD_varVal_SELFLOOPS = false;
		this.ADD_ALL_varVal_SELFLOOPS = false;
//		this.PRINT_SYSIM_TEST_ADAPTER = false;
		this.SEQ_EXECUTION = false;
		this.SEQ_EXECUTION_TOGGLES = false;
		this.USE_SYNCHRONOUS_PORTS = false;
		this.USE_PULSE_PACKS = true;
		this.USE_ENV_RESTRICTION = false;
		this.ADD_ABSTRACT_inEventPool_SELFLOOPS = false;
		this.ALLOWED_STATES_inState_SELFLOOPS = new ArrayList<String>();
		this.ALLOWED_EVENTS_inEventPool_SELFLOOPS = new ArrayList<String>();
		this.ALLOWED_VARS_varVal_SELFLOOPS = new ArrayList<String>();
	}
	
	public static PrintingOptions simpleOptimizations() {
		PrintingOptions result = new PrintingOptions().clear();
		result.COMBINE_SMS_ES = true;
		return result;
	}
	
	public static PrintingOptions createMinInputEnabledOptions(boolean alsoOptimizations) {
		PrintingOptions result = new PrintingOptions();
		result.COMBINE_SMS_ES = alsoOptimizations;
		return result;
	}
	
	public static PrintingOptions empty() {
		PrintingOptions result = new PrintingOptions();
		result.USE_PREPROCESSED_ASAL = false;
		result.COMBINE_SMS_ES = false;
		result.EVENT_QUEUE_BOUND = 1;
		result.ADD_inState_SELFLOOPS = false;
		result.ADD_ALL_inState_SELFLOOPS = false;
		result.ADD_inEventPool_SELFLOOPS = false;
		result.ADD_ALL_inEventPool_SELFLOOPS = false;
		result.RESET_VARIABLES_INITIAL = false;
		result.ADD_varVal_SELFLOOPS = false;
		result.ADD_ALL_varVal_SELFLOOPS = false;
//		result.PRINT_SYSIM_TEST_ADAPTER = false;
		result.SEQ_EXECUTION = false;
		result.SEQ_EXECUTION_TOGGLES = false;
		result.USE_PULSE_PACKS = false;
		result.USE_ENV_RESTRICTION = false;
		result.ADD_ABSTRACT_inEventPool_SELFLOOPS = false;
		result.ALLOWED_STATES_inState_SELFLOOPS = new ArrayList<String>();
		result.ALLOWED_EVENTS_inEventPool_SELFLOOPS = new ArrayList<String>();
		result.ALLOWED_VARS_varVal_SELFLOOPS = new ArrayList<String>();
		return result;
	}
	
	public PrintingOptions clear() {
		this.USE_PREPROCESSED_ASAL = false;
		this.COMBINE_SMS_ES = false;
		this.EVENT_QUEUE_BOUND = 1;
		this.ADD_inState_SELFLOOPS = false;
		this.ADD_ALL_inState_SELFLOOPS = false;
		this.ADD_inEventPool_SELFLOOPS = false;
		this.ADD_ALL_inEventPool_SELFLOOPS = false;
		this.RESET_VARIABLES_INITIAL = false;
		this.ADD_varVal_SELFLOOPS = false;
		this.ADD_ALL_varVal_SELFLOOPS = false;
//		this.PRINT_SYSIM_TEST_ADAPTER = false;
		this.SEQ_EXECUTION = false;
		this.SEQ_EXECUTION_TOGGLES = false;
		this.USE_SYNCHRONOUS_PORTS = false;
		this.USE_PULSE_PACKS = false;
		this.USE_ENV_RESTRICTION = false;
		this.ADD_ABSTRACT_inEventPool_SELFLOOPS = false;
		this.ALLOWED_STATES_inState_SELFLOOPS = new ArrayList<String>();
		this.ALLOWED_EVENTS_inEventPool_SELFLOOPS = new ArrayList<String>();
		this.ALLOWED_VARS_varVal_SELFLOOPS = new ArrayList<String>();
		return this;
	}
}
