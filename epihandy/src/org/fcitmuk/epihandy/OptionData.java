package org.fcitmuk.epihandy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.fcitmuk.db.util.Persistent;


/**
 * 
 * @author Daniel
 *
 */
public class OptionData  implements Persistent {
	
	private short id = EpihandyConstants.NULL_ID;
	private OptionDef def;
	
	public OptionData(){
		
	}

	/** Copy constructor. */
	public OptionData(OptionData data){
		setId(data.getId());
		setDef(new OptionDef(data.getDef()));
	}
	
	public OptionData( OptionDef def) {
		setDef(def);
		setId(def.getId());
	}

	public short getId() {
		return id;
	}

	public void setId(short id) {
		this.id = id;
	}
	
	public OptionDef getDef() {
		return def;
	}

	public void setDef(OptionDef def) {
		this.def = def;
	}

	public void read(DataInputStream dis) throws IOException {
		setId(dis.readShort());
	}

	public void write(DataOutputStream dos) throws IOException {
		dos.writeShort(getId());
	}
	
	public String toString() {
		return getDef().getText();
	}
	
	public String getValue(){
		return getDef().getVariableName();
	}
}
