package com.llui.idrink.Enums;

import androidx.annotation.NonNull;
/**
 * DataType enum represents different types of data formats.
 * Each enum constant provides a toString() method for display purposes
 * and a getFileExtension() method to return the corresponding file extension.
 * This is used when writing and creating files.
 */

public enum DataType {
    AUDIO {
        @NonNull
        @Override
        public String toString() {
            return "audio";
        }

        @Override
        public String getFileExtension() {
            return ".pcm"; // Example extension for audio files
        }
    },
    VIDEO {
        @NonNull
        @Override
        public String toString() {
            return "video";
        }

        @Override
        public String getFileExtension() {
            return ".mp4"; // Example extension for video files
        }
    },
    IMU {
        @NonNull
        @Override
        public String toString() {
            return "IMU";
        }

        @Override
        public String getFileExtension() {
            return ".csv"; // Example extension for IMU data files
        }
    },
    POSITION {
        @NonNull
        @Override
        public String toString() {
            return "position";
        }

        @Override
        public String getFileExtension() {
            return ".csv"; // Example extension for position data files
        }
    },
    JSON {
        @NonNull
        @Override
        public String toString() {
            return "doc";
        }

        @Override
        public String getFileExtension() {
            return ".json"; // Example extension for JSON files
        }
    };

    // Abstract method to be implemented by each enum constant
    public abstract String getFileExtension();
}

