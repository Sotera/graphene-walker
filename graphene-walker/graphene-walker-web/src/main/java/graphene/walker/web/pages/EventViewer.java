package graphene.walker.web.pages;

import graphene.dao.TransactionDAO;
import graphene.model.idl.G_VisualType;
import graphene.model.query.EventQuery;
import graphene.model.view.events.DirectedEventRow;
import graphene.util.ExceptionUtil;
import graphene.util.validator.ValidationUtils;
import graphene.web.annotations.PluginPage;
import graphene.web.model.DirectedEventDataSource;
import graphene.web.pages.SimpleBasePage;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.slf4j.Logger;

@PluginPage(visualType = G_VisualType.EXPERIMENTAL, menuName = "Event Viewer", icon = "fa fa-lg fa-fw fa-cogs")
public class EventViewer extends SimpleBasePage {

	// Handle event "selected"
	// private enum Mode {
	// ACCOUNT,
	// CUSTOMER;
	// }

	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;

	@Inject
	private BeanModelSource beanModelSource;

	@Persist
	@Property
	private String currentEntity;

	@Inject
	private TransactionDAO dao;
	//
	// private Mode drillDown;

	@Property
	private DirectedEventRow drillDownevent;

	private String drillDownId;

	@Property
	private List<DirectedEventRow> events;

	@Property
	private final GridDataSource gds = new DirectedEventDataSource(dao);

	// @InjectComponent
	// private Zone drillDownZone;

	@Property
	@Persist
	private boolean highlightZoneUpdates;

	@Property
	private DirectedEventRow currentEvent;

	// /////////////////////////////////////////////////////////////////////
	// FILTER
	// /////////////////////////////////////////////////////////////////////

	@InjectComponent
	private Zone listZone;

	@Inject
	private Logger logger;

	@Inject
	private AlertManager alertManager;

	@Inject
	private Messages messages;

	private String previousId;

	@Inject
	private Request request;

	@Persist
	@Property
	private String searchValue;

	@Property
	private DirectedEventRow selectedEvent;

	public Format getDateFormat() {
		return new SimpleDateFormat(getDatePattern());
	}

	public String getDatePattern() {
		return "dd/MM/yyyy";
	}

	public BeanModel getModel() {
		final BeanModel<DirectedEventRow> model = beanModelSource
				.createEditModel(DirectedEventRow.class, messages);
		model.exclude("comments", "credit", "dateMilliSeconds",
				"day_one_based", "debit", "localUnitBalance", "unit",
				"unitBalance", "year", "receiverId", "senderId", "id");
		model.add("action", null);
		model.add("senderName", null);
		model.add("receiverName", null);
		model.reorder("action", "date", "senderName", "receiverName");
		return model;
	}

	/**
	 * @return the receiverName
	 */
	public final String getReceiverName() {
		return currentEvent.getData().get("receiverValue");

	}

	/**
	 * @return the senderName
	 */
	public final String getSenderName() {
		return currentEvent.getData().get("senderValue");
	}

	public String getZoneUpdateFunction() {
		return highlightZoneUpdates ? "highlight" : "show";
	}

	void onActivate(final String searchValue) {
		this.searchValue = searchValue;
	}

	String onPassivate() {
		return searchValue;
	}

	void onSuccessFromFilterForm() {
		if ((events == null) || events.isEmpty()
				|| !previousId.equalsIgnoreCase(searchValue)) {
			// don't use cached version.
			final EventQuery q = new EventQuery();
			q.addId(searchValue);
			try {
				// FIXME: Need to set limit and offset in query object
				events = dao.getEvents(q);
			} catch (final Exception ex) {
				// record error to screen!
				final String message = ExceptionUtil.getRootCauseMessage(ex);
				alertManager.alert(Duration.SINGLE, Severity.ERROR, "ERROR: "
						+ message);
				logger.error(message);
				events = new ArrayList<DirectedEventRow>();
			}
			previousId = searchValue;
		}
		if (request.isXHR()) {
			logger.debug("Rendering AJAX response");
			ajaxResponseRenderer.addRender(listZone);
		}
	}

	/**
	 * This should help with persisted values.
	 */
	void setupRender() {
		if (ValidationUtils.isValid(searchValue)) {
			final EventQuery e = new EventQuery();
			e.addId(searchValue);
			try {
				events = dao.getEvents(e);
			} catch (final Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else {
			events = new ArrayList();
		}

	}

}
