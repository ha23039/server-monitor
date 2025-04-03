package com.monitoring.server.views.databases;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Databases")
@Route("databases")
@Menu(order = 2, icon = LineAwesomeIconUrl.DATABASE_SOLID)
public class DatabasesView extends Composite<VerticalLayout> {

    public DatabasesView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        Icon icon = new Icon();
        H1 h1 = new H1();
        Button buttonPrimary = new Button();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow);
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("50px");
        icon.setIcon("lumo:user");
        icon.setWidth("50px");
        icon.setHeight("50px");
        h1.setText("Bases de Datos Monitoreadas");
        h1.setWidth("max-content");
        buttonPrimary.setText("AGREGAR DB");
        buttonPrimary.getStyle().set("flex-grow", "1");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getContent().add(layoutRow);
        layoutRow.add(icon);
        layoutRow.add(h1);
        layoutRow.add(buttonPrimary);
    }
}
