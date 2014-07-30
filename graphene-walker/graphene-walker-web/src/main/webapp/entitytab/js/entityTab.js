Ext.define("DARPA.EntityTab", {

	extend:'Ext.tab.Panel',
//	extend:'Ext.Container',
	layout: {
		type:'vbox',
		align:'stretch'
	},
	items:[],
	entity:{},
	constructor:function(config) {
	                
		var details = Ext.create("DARPA.EntityDetailsFrame", {
			title:'ENTITY DETAILS',
			id:config.id + '-Details',			
			entityname:config.title, 
                        institution: config.institution,
			entity:config.entity});
/*			
// Only showing transfers for Walker
		var transactions = Ext.create("DARPA.Ledger", {
			title:'TRANSACTIONS',
			id:config.id + "-Ledger",
                        institution: config.institution,
			disabled:true
			
		});
*/
		var transfers = Ext.create("DARPA.TransfersLedger", {
			title:'EMAILS',
			id:config.id + '-Transfers',
                        institution: config.institution,
			disabled:true			
		});

		var transactionGraph = Ext.create("DARPA.TransactionGraph", {
			title:'EMAIL GRAPH', 
			id:config.id + '-TransGraph',
			entityId: config.id, 	
                        institution: config.institution,
			disabled:true			
		});
		
		var entityGraph = Ext.create("DARPA.PBGraph", {
			title:'ENTITY GRAPH', 
			entityId: config.id, 
			id:config.id + '-EntityGraph',
                        institution: config.institution
		});

		this.items = [
		details, 
//		transactions, 
		transfers, 
		transactionGraph, 
		entityGraph
		];

		this.callParent(arguments);
		
		/* Graphene-Walker does not have a shared attribute entity graph */
		entityGraph.load(config.id);
		if (entityGraph.prevLoadParams) {
			entityGraph.prevLoadParams.prevNumber = config.id;
		}
		else {
			entityGraph.prevLoadParams = {prevNumber: config.id};
		}
	},
/*	
	loadLedger:function(account) { 
		var t = this.getTransactions();
		t.getAllTransactions(account);
		t.setDisabled(false);
		this.setActiveTab(t);
	},
*/	
	loadTransfers:function(account) {
		var t = this.getTransfers();
		t.getAllTransactions(account);
		t.setDisabled(false);		
	},
	
	loadTransactionGraph:function(accountNumber, hasData) {
		var self=this;
		var t = self.getTransactionGraph();
		t.setDisabled(!hasData);		
		if (hasData)
			t.load([accountNumber]);
	},

	getDetailsTab:function() 
	{
		return Ext.getCmp(this.id+'-Details');
	},
/*	
	getTransactions:function() 
	{
		return this.items.items[1];
	},
*/	
	getTransfers:function() 
	{
		return Ext.getCmp(this.id+'-Transfers');
	},
	getTransactionGraph:function()
	{
		return Ext.getCmp(this.id+'-TransGraph');
	},
	getEntityGraph:function()
	{
		// note: this should return undefined, since Graphene-Walker does not have an entity graph
		var self = this;
		return Ext.getCmp(this.id+'-EntityGraph');
	}

});