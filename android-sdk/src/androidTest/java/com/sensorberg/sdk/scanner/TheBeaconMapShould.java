package com.sensorberg.sdk.scanner;

import android.support.test.runner.AndroidJUnit4;

import com.sensorberg.sdk.model.BeaconId;
import com.sensorberg.sdk.internal.Clock;
import com.sensorberg.sdk.internal.FileHelper;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class TheBeaconMapShould {

    BeaconMap tested;
    private Clock clock ;

    @Before
    public void setUp() throws Exception {
        clock = new Clock() {
            @Override
            public long now() {
                return 0;
            }

            @Override
            public long elapsedRealtime() {
                return 0;
            }
        };
    }

    @Test
    public void be_able_written_to_a_file() throws IOException {
        File file = getTempFile();
        tested = new BeaconMap(file);

        long firstSize = file.length();

        tested.put(getNewBeaconId(), new EventEntry(System.currentTimeMillis(), ScanEventType.ENTRY.getMask()));


        Assertions.assertThat(firstSize).isNotEqualTo(file.length());
    }

    private File getTempFile() throws IOException {
        return File.createTempFile("test" + System.currentTimeMillis(), null);
    }

    @Test
    public void not_fail_if_file_is_corrupted() throws IOException {
        File tempFile = getTempFile();
        FileHelper.write(10000L, tempFile);

        tested = new BeaconMap(tempFile);

        Assertions.assertThat(tested.size()).isEqualTo(0);
    }

    @Test
    public void be_restored_from_a_prviously_serialized_beacon_map() throws IOException {
        File file = getTempFile();
        BeaconMap first = new BeaconMap(file);

        first.put(getNewBeaconId(), new EventEntry(System.currentTimeMillis(), ScanEventType.ENTRY.getMask()));

        tested = new BeaconMap(file);

        Assertions.assertThat(tested.size()).isEqualTo(1);
        Assertions.assertThat(tested.get(getNewBeaconId())).isNotNull();
    }

    private BeaconId getNewBeaconId() {
        return new BeaconId(UUID.fromString("D57092AC-DFAA-446C-8EF3-C81AA22815B5"), 1, 1);
    }

    @Test
    public void persist_file_after_filter() throws IOException {
        File file = getTempFile();
        BeaconMap first = new BeaconMap(file);

        first.put(getNewBeaconId(), new EventEntry(clock.now(), ScanEventType.ENTRY.getMask()));

        long originalSize = file.length();

        tested = new BeaconMap(file);
        Assertions.assertThat(tested.size()).isEqualTo(1);

        tested.filter(new BeaconMap.Filter() {
            @Override
            public boolean filter(EventEntry beaconEntry, BeaconId beaconId) {
                return true;
            }
        });

        Assertions.assertThat(file.length()).isLessThan(originalSize);
    }

    @Test
    public void remove_entries_that_match_the_filter() throws IOException {
        tested = new BeaconMap(getTempFile());
        tested.put(getNewBeaconId(), new EventEntry(System.currentTimeMillis(), ScanEventType.ENTRY.getMask()));
        Assertions.assertThat(tested.size()).isEqualTo(1);

        tested.filter(new BeaconMap.Filter() {
            @Override
            public boolean filter(EventEntry beaconEntry, BeaconId beaconId) {
                return true;
            }
        });
        Assertions.assertThat(tested.size()).isEqualTo(0);
    }

    @Test
    public void not_fail_if_file_does_not_exist() throws IOException {
        File tempFile = getTempFile();
        tempFile.delete();
        try {
            tested = new BeaconMap(tempFile);
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void should_be_readable_right_after_writing() throws Exception {
        File tempFile = getTempFile();
        tested = new BeaconMap(tempFile);
        tested.put(getNewBeaconId(), new EventEntry(clock.now(), ScanEventType.ENTRY.getMask()));

        BeaconMap otherFile = new BeaconMap(tempFile);
        Assertions.assertThat(otherFile).isNotNull();
        Assertions.assertThat(otherFile.size()).isEqualTo(1);
    }
}
