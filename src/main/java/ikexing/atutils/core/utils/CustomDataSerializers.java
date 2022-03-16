package ikexing.atutils.core.utils;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CustomDataSerializers {
    public static final DataSerializer<Set<BlockPos>> SERIALIZER_BLOCK_POS_SET = new DataSerializer<Set<BlockPos>>() {
        public void write(PacketBuffer buf, Set<BlockPos> value) {
            buf.writeVarInt(value.size());
            for (BlockPos pos : value) {
                buf.writeBlockPos(pos);
            }
        }

        public Set<BlockPos> read(PacketBuffer buf) throws IOException {
            int length = buf.readVarInt();
            Set<BlockPos> result = new HashSet<>(length);
            for (int i = 0; i < length; i++) {
                result.add(buf.readBlockPos());
            }
            return result;
        }

        public DataParameter<Set<BlockPos>> createKey(int id) {
            return new DataParameter<>(id, this);
        }

        public Set<BlockPos> copyValue(Set<BlockPos> value) {
            return new HashSet<>(value);
        }
    };
}
