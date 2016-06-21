package the_fireplace.ias.legacysupport;

import net.minecraft.client.resources.I18n;

public class OldJava implements ILegacyCompat {
	@Override
	public int[] getDate() {
		int[] ret = new int[3];
		ret[0]=0;
		ret[1]=0;
		ret[2]=0;
		return ret;
	}

	@Override
	public String getFormattedDate() {
		return I18n.format("ias.updatejava");
	}

}
