#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash, Ord, PartialOrd)]
pub struct Point {
    x: i32,
    y: i32,
}

impl Point {
    pub fn new(x: i32, y: i32) -> Self {
        Self { x, y }
    }

    /// A utility function for indexing into a grid using the x coordinate
    /// of the point. It wraps the conversion from i32 to usize
    pub fn x(&self) -> usize {
        assert!(self.x >= 0, "Index into an array has to be non-negative");
        self.x as usize
    }

    /// A utility function for indexing into a grid using the y coordinate
    /// of the point. It wraps the conversion from i32 to usize
    pub fn y(&self) -> usize {
        assert!(self.y >= 0, "Index into an array has to be non-negative");
        self.y as usize
    }

    pub fn translate_left(&self) -> Point {
        Self {
            x: self.x - 1,
            y: self.y,
        }
    }

    pub fn translate_right(&self) -> Point {
        Self {
            x: self.x + 1,
            y: self.y,
        }
    }

    pub fn translate_up(&self) -> Point {
        Self {
            x: self.x,
            y: self.y - 1,
        }
    }

    pub fn translate_down(&self) -> Point {
        Self {
            x: self.x,
            y: self.y + 1,
        }
    }

    pub fn get_neighbours(&self) -> Vec<Point> {
        vec![
            self.translate_up(),
            self.translate_right(),
            self.translate_down(),
            self.translate_left(),
        ]
    }

    pub fn is_within_grid<T>(&self, grid: &Vec<Vec<T>>) -> bool {
        grid.len() > 0
            && grid[0].len() > 0
            && self.x >= 0
            && self.y >= 0
            && (self.y as usize) < grid.len()
            && (self.x as usize) < grid[0].len()
    }
}
