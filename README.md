# Diff Tool

A Java application for comparing directories and files, featuring both a Graphical User Interface (Swing) and a Terminal User Interface (Lanterna).

![3.png](z_readme%20images/3.png)   
![4.png](z_readme%20images/4.png)

## Project Structure

* **Core Logic (`algorithms/`)**
    * **`HuntMcIlroy.java`**: Implements the Hunt-McIlroy algorithm for line-by-line file comparison and Longest Common Subsequence calculation.
    * **`LevenshteinDistance.java`**: Calculates edit distances for inline character comparison within modified lines.
    * **`BinaryHeuristics.java`**: Detects binary files (ZIP, PDF, PNG, etc.) to prevent text comparison errors.
    * **`FileUtils.java`**: Manages file reading, comparison orchestration, and exporting results to Text or HTML.
* **Terminal UI (`lanterna/`)**
    * **`LanternaInterface.java`**: The main controller for the text-based interface, handling directory navigation and split-screen diff views.
* **Graphical UI (`swing/`)**
    * **`SwingInterface.java`**: The main Swing GUI controller featuring a 3-level navigation system (Directory -> File List -> Diff View).

## Features

* **Dual Interfaces:** Automatically detects environment; supports headless mode (CUI) and desktop mode (GUI).
* **Directory Comparison:** Side-by-side view of two directories with sorting options (Name, Size, Date) and search filtering.
* **Visual Diffing:**
    * Highlights line differences using `!` (Modified), `+` (Added), and `-` (Removed) markers.
    * Performs inline character comparison if lines are at least 70% similar.
* **Export:** Save comparison results as `.txt` or `.html` reports.
* **Extras:** Includes embedded minigames (TicTacToe, Blackjack).

## Usage

1.  **Select Directories:** Input paths for two directories to compare.
    ![1.png](z_readme%20images/1.png)
2.  **Select Files:** Choose a file from the list. Files present in both directories are marked as "Identical" or "Different".
    ![2.png](z_readme%20images/2.png)
3.  **View Diff:** The tool displays the files side-by-side.
    * **Green:** Content exists only in the selected file.
    * **Red:** Content exists only in the comparison file.
    * **Orange/Yellow:** Line exists in both but has modifications.

    ![3.png](z_readme%20images/3.png)   
    ![4.png](z_readme%20images/4.png)

## License/Copyright
© 2025 Benedikt Belschner, Daniel Rodean, Colin Traub, Finn Wolf
All Rights Reserved.