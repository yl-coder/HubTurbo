package ui;

import java.util.function.Function;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

public class EditableLabel extends HBox {

	private final TextField textField = new TextField();
	private final Label label = new Label();
	
	private Function<String, String> postEditTransformation = (s) -> s;
	private Function<String, String> translation = (s) -> s;
	private Runnable cancel = () -> {};
	private String previousText = "";
	
	public EditableLabel(String initialText) {
		
		setup(initialText);
	}

	private void setup(String initialText) {
		textField.setPrefColumnCount(30);
		label.setPrefWidth(300);
		label.setText(initialText);

		getChildren().add(label);
		
		label.setOnMouseClicked(e -> {
			triggerEdit();
		});
		
		textField.setOnKeyReleased(e -> {
			if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.ESCAPE) {
				getChildren().clear();
				getChildren().add(label);
				if (e.getCode() == KeyCode.ENTER) {
					textField.setText(postEditTransformation.apply(textField.getText()));
					setLabelText(textField.getText());
				} else {
					textField.setText(previousText);
					cancel.run();
				}
			}
		});
		
		textField.focusedProperty().addListener((obs, old, newValue) -> {
			if (!newValue) { // on losing focus
				textField.setText(previousText);
				cancel.run();
			}
		});
	}

	public void triggerEdit() {
		getChildren().clear();
		previousText = textField.getText();
		getChildren().add(textField);
		Platform.runLater(() -> textField.requestFocus());
	}
	
	private void setLabelText(String text) {
		label.setText(translation.apply(text));
	}

	public void setTextFieldText(String text) {
		textField.setText(text);
		setLabelText(text);
	}

	public EditableLabel setEditTransformation(Function<String, String> postEditTransformation) {
		this.postEditTransformation = postEditTransformation;
		return this;
	}

	public EditableLabel setTranslationFunction(Function<String, String> translationFunction) {
		this.translation = translationFunction;
		return this;
	}

	public EditableLabel setOnCancel(Runnable cancel) {
		this.cancel = cancel;
		return this;
	}
}
