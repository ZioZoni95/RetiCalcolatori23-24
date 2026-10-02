package hotelier.client.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/** Finestra modale con più campi di testo, usata dalla TUI per raccogliere i dati dei comandi. */
final class TuiForm {

    record Field(String label, String initial, boolean secret) {
        static Field text(String label) {
            return new Field(label, "", false);
        }

        static Field secret(String label) {
            return new Field(label, "", true);
        }
    }

    private TuiForm() {
    }

    /** I valori inseriti, oppure vuoto se l'utente annulla. Va chiamato dal thread dell'interfaccia. */
    static Optional<List<String>> show(MultiWindowTextGUI gui, String title, List<Field> fields) {
        BasicWindow window = new BasicWindow(title);
        window.setHints(List.of(Window.Hint.CENTERED));
        AtomicReference<List<String>> result = new AtomicReference<>();

        Panel grid = new Panel(new GridLayout(2));
        List<TextBox> boxes = new ArrayList<>();
        for (Field field : fields) {
            grid.addComponent(new Label(field.label()));
            TextBox box = new TextBox(new TerminalSize(34, 1), field.initial());
            if (field.secret()) {
                box.setMask('*');
            }
            boxes.add(box);
            grid.addComponent(box);
        }

        Panel buttons = new Panel(new LinearLayout(Direction.HORIZONTAL));
        buttons.addComponent(new Button("OK", () -> {
            result.set(boxes.stream().map(box -> box.getText().strip()).toList());
            window.close();
        }));
        buttons.addComponent(new Button("Annulla", window::close));

        Panel root = new Panel(new LinearLayout(Direction.VERTICAL));
        root.addComponent(grid);
        root.addComponent(buttons);
        window.setComponent(root);
        gui.addWindowAndWait(window);
        return Optional.ofNullable(result.get());
    }
}
