/* Copyright (c) 2008-2009 Sascha Kohlmann - all rights reserved */
package net.sportics.dni.rt.client.microedition.accu;

import net.sportics.dni.rt.client.microedition.accu.Accumulator;
import net.sportics.dni.rt.client.microedition.accu.Assemblage;
import net.sportics.dni.rt.client.microedition.accu.Sink;
import net.sportics.dni.rt.client.microedition.TypedData;
import net.sportics.dni.rt.client.microedition.TypedDataConsumer;
import net.sportics.dni.rt.client.microedition.TypedDataDescriptor;
import net.sportics.dni.rt.client.microedition.TypedDataProducer;
import net.sportics.dni.rt.client.microedition.Variant;
import junit.framework.TestCase;

public class AccumulatorTest extends TestCase {

    public void testAccuWithSourceAndSink() throws Exception {
        final TestSink sink = new TestSink();
        final Accumulator accu = Accumulator.getInstance();
        accu.registerSink(sink);
        final TestSource source = new TestSource();
        source.registerTypedDataConsumer(accu);

        source.fireTestData1();
        Thread.sleep(1100);

        final Assemblage assemble = sink.getToSink();
//        System.out.println("assemble:  " + assemble);
        assertNotNull(assemble);
        final TypedData ts = assemble.get(TypedDataDescriptor.STRIDES);
        final Variant vs = ts.getValue();
        final int steps = vs.asInteger();
        assertEquals(2, steps);

        final TypedData tsp = assemble.get(TypedDataDescriptor.SPEED);
        final Variant vsp = tsp.getValue();
        final float speed = vsp.asFloat();
        assertEquals(Float.floatToIntBits(1.2f), Float.floatToIntBits(speed));

        source.fireTestData2();
        Thread.sleep(1100);

        final Assemblage assemble2 = sink.getToSink();
//        System.out.println("assemble2: " + assemble2);
        assertNotNull(assemble2);
        final TypedData ts2 = assemble2.get(TypedDataDescriptor.STRIDES);
        final Variant vs2 = ts2.getValue();
        final int steps2 = vs2.asInteger();
        assertEquals(5, steps2);

        final TypedData tsp2 = assemble2.get(TypedDataDescriptor.SPEED);
        final Variant vsp2 = tsp2.getValue();
        final float speed2 = vsp2.asFloat();
        assertEquals(Float.floatToIntBits(2.2f), Float.floatToIntBits(speed2));
    }

    public void testSimple() {
        
    }

    private static final class TestSource implements TypedDataProducer {

        private TypedDataConsumer accu;
        public void registerTypedDataConsumer(final TypedDataConsumer collector) {
            this.accu = collector;
        }

        public void unregisterTypedDataConsumer(final TypedDataConsumer collector) {
        }

        public void fireTestData1() {
//            System.out.println("######### fireTestData1 - start #########");
            final TypedData speed = new TypedData(TypedDataDescriptor.SPEED, new Variant(1.2f));
            this.accu.newData(this, speed);
            this.accu.newData(this, new TypedData(TypedDataDescriptor.STRIDES, new Variant(2)));
//            System.out.println("######### fireTestData1 - end #########");
        }

        public void fireTestData2() {
//            System.out.println("######### fireTestData2 - start #########");
            final TypedData speed = new TypedData(TypedDataDescriptor.SPEED, new Variant(2.2f));
            this.accu.newData(this, speed);
            this.accu.newData(this, new TypedData(TypedDataDescriptor.STRIDES, new Variant(5)));
//            System.out.println("######### fireTestData2 - end #########");
        }
    }

    private static final class TestSink implements Sink {

        private Assemblage toSink;
        public void sink(final Assemblage toSink) {
            this.toSink = toSink;
        }
        public Assemblage getToSink() {
            return this.toSink;
        }
    }
}
