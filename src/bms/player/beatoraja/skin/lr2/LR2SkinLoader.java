package bms.player.beatoraja.skin.lr2;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;

import bms.player.beatoraja.MainState;
import bms.player.beatoraja.skin.BooleanPropertyFactory;
import bms.player.beatoraja.skin.SkinLoader;
import bms.player.beatoraja.skin.SkinObject.BooleanProperty;
import bms.player.beatoraja.skin.SkinPropertyMapper;

public class LR2SkinLoader extends SkinLoader {

	private Array<Command> commands = new Array<Command>();

	protected IntIntMap op = new IntIntMap();

	protected void addCommandWord(Command cm) {
		commands.add(cm);
	}

	protected void addCommandWord(Command... cm) {
		commands.addAll(cm);
	}

	boolean skip = false;
	boolean ifs = false;

	protected void processLine(String line, MainState state) {
		if (!line.startsWith("#") ) {
			return;
		}
		String[] str = line.split(",", -1);
		if (str.length > 0) {
			if (str[0].equals("#IF")) {
				ifs = true;
				for (int i = 1; i < str.length; i++) {
					boolean b = false;
					if (str[i].length() == 0) {
						continue;
					}
					try {
						int opt = Integer.parseInt(str[i].replace('!', '-').replaceAll("[^0-9-]", ""));
						if(opt >=  0) {
							if(op.get(opt, -1) == 1) {
								b = true;
							}
						} else {
							if(op.get(-opt, -1) == 0) {
								b = true;
							}
						}
						if (!b && !op.containsKey(Math.abs(opt)) && state != null) {
							BooleanProperty draw = BooleanPropertyFactory.getBooleanProperty(opt);
							if(draw != null) {
								b = draw.get(state);								
							}
						}
						if (!b) {
							ifs = false;
							break;
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
						break;
					}
				}

				skip = !ifs;
			} else if (str[0].equals("#ELSEIF")) {
				if (ifs) {
					skip = true;
				} else {
					ifs = true;
					for (int i = 1; i < str.length; i++) {
						boolean b = false;
						try {
							int opt = Integer.parseInt(str[i].replace('!', '-').replaceAll("[^0-9-]", ""));
							if(opt >=  0) {
								if(op.get(opt, -1) == 1) {
									b = true;
								}
							} else {
								if(op.get(-opt, -1) == 0) {
									b = true;
								}
							}
							if (!b && !op.containsKey(Math.abs(opt)) && state != null) {
								BooleanProperty draw = BooleanPropertyFactory.getBooleanProperty(opt);
								if(draw != null) {
									b = draw.get(state);								
								}
							}
							if (!b) {
								ifs = false;
								break;
							}
						} catch (NumberFormatException e) {
							break;
						}
					}

					skip = !ifs;
				}
			} else if (str[0].equals("#ELSE")) {
				skip = ifs;
			} else if (str[0].equals("#ENDIF")) {
				skip = false;
				ifs = false;
			}
			if (!skip) {
				if (str[0].equals("#SETOPTION")) {
					int index = Integer.parseInt(str[1]);
					op.put(index, Integer.parseInt(str[2]) >= 1 ? 1 : 0);
				}

				Command command = null;
				for (Command cm : commands) {
					if (str[0].substring(1).equals(cm.name())) {
						command = cm;
						break;
					}
				}
				if(command != null) {
					command.execute(this, str);					
				}
			}
		}
	}

	public IntIntMap getOption() {
		return op;
	}

	public abstract class CommandWord implements Command<LR2SkinLoader> {

		public final String str;

		public String name() {
			return str;
		}
		
		public CommandWord(String str) {
			this.str = str;
		}

		public void execute(LR2SkinLoader loader, String[] values) {
			execute(values);
		}

		public abstract void execute(String[] values);

	}
	
	public interface Command<T extends LR2SkinLoader> {
		
		public abstract String name();
		public abstract void execute(T loader, String[] values);		
	}
}
