package ikexing.atutils.asm.common;


import ikexing.atutils.asm.ATUtilsCore;
import ikexing.atutils.asm.transformers.ChoppableTransformer;
import ikexing.atutils.asm.utils.SafeClassWriter;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ATUtilsTransformer implements IClassTransformer {
    private static final Map<String, Consumer<ClassNode>> tweakedClasses = new HashMap<>();

    static {
        tweakedClasses.put("gigaherz.survivalist.api.Choppable$ChoppingRecipe", ChoppableTransformer::transform);
    }


    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!tweakedClasses.containsKey(transformedName)) {
            return basicClass;
        }

        ATUtilsCore.logger.info("Transforming: " + transformedName);
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, ClassReader.SKIP_FRAMES);

            Consumer<ClassNode> consumer = tweakedClasses.get(transformedName);
            if(consumer != null) {
                consumer.accept(classNode);

                ClassWriter classWriter = new SafeClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                classNode.accept(classWriter);
                return classWriter.toByteArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return basicClass;
    }
}
