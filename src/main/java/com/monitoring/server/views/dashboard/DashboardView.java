package com.monitoring.server.views.dashboard;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Dashboard")
@Route("dashboard")
@Menu(order = 1, icon = LineAwesomeIconUrl.CHART_AREA_SOLID)
public class DashboardView extends Composite<VerticalLayout> {

    public DashboardView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
