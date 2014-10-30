DARPA.TXNGNodeDisplay.prototype.setAttrs = function(element) {
	var self = this;
	var html = "<table id='node_details_table' rules='rows'>";
	var data = element.data();
	var attrs = data.attrs;
	var detailsItems = self.items.items[0].items.items; // MFM
	var elementIsEdge = element.isEdge();
	
	for (var prop in data) {
		if (data.hasOwnProperty(prop)) {
			if (prop == "idType") {
				detailsItems[0].setValue(data[prop]);
			}
			else if (prop == "idVal") {
				detailsItems[1].setValue(data[prop]);
			}
		}
	}
	
	for (var i = 0; i < attrs.length; ++i) {
		var a = attrs[i];
		if (elementIsEdge) {
			if (a.key.indexOf("subject") != -1) {
				var id = a.key.split("_")[1];
				var condition = (typeof id == "undefined") ? "pairId" : "pairId_" + id;
				var pairId = "";
				attrs.forEach(function(_this) {
					if (_this.key == condition) {
						pairId = _this.val;
						return;
					}
				});
				html += "<tr>" +
					/*0*/ "<td>Subject</td>"+
					/*1*/ "<td>&nbsp;:&nbsp;</td>" +
					/*2*/ "<td>" + a.val + "</td>" + 
					/*3*/ "<td style='visibility:hidden;'>"+ condition +"</td>" + 
					/*4*/ "<td style='visibility:hidden;'>" + pairId + "</td>"+ 
				"</tr>";
			}
		} else {
			html += "<tr><td>" + a.key + "</td> <td>&nbsp;:&nbsp;</td> <td>" + a.val + "</td></tr>";
		}
	}
	html += "</table>";
	if (elementIsEdge) {
		html = "<span style='color:blue'>[Click on a row in this table to view that email]</span><br><br>" + html; 
	}
	detailsItems[2].update(html);
	
	// I know this is some hideous code and not my usual style, but...
	// it works
	document.getElementById("node_details_table").onclick = function(e) {
		if (!elementIsEdge) return;
		e = e || window.event; //for IE87 backward compatibility
		var t = e.target || e.srcElement; //IE87 backward compatibility
		while (t.nodeName != 'TD' && t.nodeName != 'TH' && t.nodeName != 'TABLE') {
			t = t.parentNode;
		}
		if (t.nodeName == 'TABLE') {
			return;
		}
		var pairId = t.parentNode.cells[4].innerHTML;
		var subject = t.parentNode.cells[2].innerHTML;
		var rowId = t.parentNode.cells[3].innerHTML.split("_")[1];
		rowId = (typeof rowId == "undefined") ? "" : "_" + rowId;
		var payload = "";
		var edge; 
		self.getGraph().GraphVis.gv.edges().each(function(i, e) {
			var edgeAttrs = e.data().attrs;
			var pairId_found = false;
			var payload_found = false;
			
			edgeAttrs.forEach(function(_this, index) {
				var key = "pairId" + rowId;
				if (_this.key == key && _this.val == pairId) {
					pairId_found = true;
					return;
				}
			});
			
			if (pairId_found) {
				edgeAttrs.forEach(function(_this, index) {
					var key = "payload" + rowId;
					if (_this.key == key) {
						payload_found = true;
						payload = _this.val;
						return;
					}
				});
			}
			
			if (pairId_found && payload_found) {
				edge = e;
				return;
			}
		});
		
		if (typeof edge != "undefined") {
			var window = Ext.getCmp("darpa-email-viewer");
			
			if (typeof window == "undefined") {
				// TODO: Find a way to get the timestamp, ugh...
				window = Ext.create("DARPA.DetailsViewer", { timeStamp: null });
			}
			
			window.show();
			
			window.setTo( self.getGraph().GraphVis.gv.$("node[id = '" + edge.data("target") +"']").data().name );
			window.setFrom( self.getGraph().GraphVis.gv.$("node[id = '" + edge.data("source") +"']").data().name );
			window.setSubject(subject);
			window.setBody(payload);
		} else {
			console.error("Unable to get email for the selected row");
		}
	};
};