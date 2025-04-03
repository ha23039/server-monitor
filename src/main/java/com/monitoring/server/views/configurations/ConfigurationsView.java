package com.monitoring.server.views.configurations;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Configurations")
@Route("configurations")
@Menu(order = 3, icon = LineAwesomeIconUrl.COG_SOLID)
public class ConfigurationsView extends Composite<VerticalLayout> {

    public ConfigurationsView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        Icon icon = new Icon();
        H1 h1 = new H1();
        TextField textField = new TextField();
        TextField textField2 = new TextField();
        TextField textField3 = new TextField();
        Button buttonPrimary = new Button();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setJustifyContentMode(JustifyContentMode.START);
        getContent().setAlignItems(Alignment.CENTER);
        layoutRow.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow);
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("50px");
        layoutRow2.setHeightFull();
        layoutRow.setFlexGrow(1.0, layoutRow2);
        layoutRow2.addClassName(Gap.MEDIUM);
        layoutRow2.setWidth("100%");
        layoutRow2.setHeight("50px");
        icon.setIcon("lumo:user");
        icon.setWidth("50px");
        icon.setHeight("50px");
        h1.setText("Configuración de Umbrales de Alerta");
        h1.setWidth("max-content");
        textField.setLabel("Umbral de CPU (%)");
        textField.setWidth("100%");
        textField2.setLabel("Umbral de RAM (%)");
        textField2.setWidth("100%");
        textField3.setLabel("Umbral de Disco (%)");
        textField3.setWidth("100%");
        buttonPrimary.setText("Guardar Configuración");
        getContent().setAlignSelf(FlexComponent.Alignment.END, buttonPrimary);
        buttonPrimary.setWidth("min-content");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getContent().add(layoutRow);
        layoutRow.add(layoutRow2);
        layoutRow2.add(icon);
        layoutRow2.add(h1);
        getContent().add(textField);
        getContent().add(textField2);
        getContent().add(textField3);
        getContent().add(buttonPrimary);
    }
}
