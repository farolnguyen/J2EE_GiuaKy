-- Insert Categories
INSERT INTO category (id, name) VALUES
(1, 'Fiction'),
(2, 'Non-Fiction'),
(3, 'Mystery & Thriller'),
(4, 'Science Fiction'),
(5, 'Romance'),
(6, 'Fantasy'),
(7, 'Biography'),
(8, 'Self-Help'),
(9, 'History'),
(10, 'Technology');
-- Insert Books
INSERT INTO book (id, author, price, title, category_id, description, discount, featured, image_url, publication_year, publisher, stock, enabled, stock_alert_threshold) VALUES

-- Fiction (5 books)
(1, 'Harper Lee', 12.99, 'To Kill a Mockingbird', 1, 'A gripping tale of racial injustice and childhood innocence in the American South.', 0.00, 1, 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=400', 1960, 'J.B. Lippincott & Co.', 25, 1, 10),
(2, 'F. Scott Fitzgerald', 10.99, 'The Great Gatsby', 1, 'A tragic love story set in the Jazz Age, exploring themes of wealth and the American Dream.', 0.10, 1, 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400', 1925, 'Charles Scribner''s Sons', 30, 1, 10),
(3, 'George Orwell', 14.99, '1984', 1, 'A dystopian novel about totalitarianism and surveillance in a future society.', 0.00, 1, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400', 1949, 'Secker & Warburg', 20, 1, 10),
(4, 'Jane Austen', 11.50, 'Pride and Prejudice', 1, 'A romantic novel exploring the complexities of love, marriage, and social class.', 0.15, 0, 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=400', 1813, 'T. Egerton', 18, 1, 10),
(5, 'Ernest Hemingway', 13.25, 'The Old Man and the Sea', 1, 'An epic struggle between an aging fisherman and a giant marlin in the Gulf Stream.', 0.00, 0, 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=400', 1952, 'Charles Scribner''s Sons', 15, 1, 10),

-- Non-Fiction (5 books)
(6, 'Yuval Noah Harari', 16.99, 'Sapiens: A Brief History of Humankind', 2, 'A groundbreaking narrative of humanity''s creation and evolution.', 0.00, 1, 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400', 2011, 'Harper', 22, 1, 10),
(7, 'Michelle Obama', 18.50, 'Becoming', 2, 'The deeply personal memoir of the former First Lady of the United States.', 0.20, 1, 'https://images.unsplash.com/photo-1592496431122-2349e0fbc666?w=400', 2018, 'Crown Publishing', 28, 1, 10),
(8, 'Malcolm Gladwell', 15.75, 'Outliers', 2, 'Exploring what makes high-achievers different through fascinating stories.', 0.00, 0, 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=400', 2008, 'Little, Brown', 12, 1, 10),
(9, 'Tara Westover', 14.99, 'Educated', 2, 'A memoir about a young woman who leaves her survivalist family to pursue education.', 0.10, 0, 'https://images.unsplash.com/photo-1519682337058-a94d519337bc?w=400', 2018, 'Random House', 16, 1, 10),
(10, 'James Clear', 17.25, 'Atomic Habits', 2, 'An easy and proven way to build good habits and break bad ones.', 0.00, 1, 'https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=400', 2018, 'Avery', 35, 1, 10),

-- Mystery & Thriller (5 books)
(11, 'Agatha Christie', 11.99, 'Murder on the Orient Express', 3, 'A classic whodunit featuring detective Hercule Poirot on a snowbound train.', 0.00, 1, 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400', 1934, 'Collins Crime Club', 20, 1, 10),
(12, 'Gillian Flynn', 13.50, 'Gone Girl', 3, 'A psychological thriller about a marriage gone terribly wrong.', 0.15, 0, 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400', 2012, 'Crown Publishing', 24, 1, 10),
(13, 'Dan Brown', 15.99, 'The Da Vinci Code', 3, 'A fast-paced thriller blending art history and conspiracy theories.', 0.00, 1, 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400', 2003, 'Doubleday', 18, 1, 10),
(14, 'Paula Hawkins', 12.75, 'The Girl on the Train', 3, 'A gripping psychological thriller about obsession and deception.', 0.10, 0, 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400', 2015, 'Riverhead Books', 14, 1, 10),
(15, 'Stieg Larsson', 14.25, 'The Girl with the Dragon Tattoo', 3, 'A journalist and a hacker investigate a decades-old disappearance.', 0.00, 0, 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=400', 2005, 'Norstedts', 19, 1, 10),

-- Science Fiction (5 books)
(16, 'Frank Herbert', 16.50, 'Dune', 4, 'An epic tale of politics, religion, and ecology on the desert planet Arrakis.', 0.00, 1, 'https://images.unsplash.com/photo-1534414899046-ac2a5e0d5b5d?w=400', 1965, 'Chilton Books', 22, 1, 10),
(17, 'Isaac Asimov', 13.99, 'Foundation', 4, 'The first book in the epic Foundation series about the fall and rise of civilizations.', 0.00, 1, 'https://images.unsplash.com/photo-1519682337058-a94d519337bc?w=400', 1951, 'Gnome Press', 17, 1, 10),
(18, 'Andy Weir', 14.75, 'The Martian', 4, 'A thrilling story of survival on Mars after an astronaut is left behind.', 0.10, 0, 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=400', 2011, 'Crown Publishing', 26, 1, 10),
(19, 'Philip K. Dick', 12.50, 'Do Androids Dream of Electric Sheep?', 4, 'A dystopian novel exploring what it means to be human.', 0.00, 0, 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=400', 1968, 'Doubleday', 11, 1, 10),
(20, 'William Gibson', 15.25, 'Neuromancer', 4, 'The groundbreaking cyberpunk novel that defined a genre.', 0.00, 0, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400', 1984, 'Ace Books', 13, 1, 10),

-- Romance (3 books)
(21, 'Nicholas Sparks', 11.99, 'The Notebook', 5, 'A timeless love story that spans decades and challenges all odds.', 0.00, 1, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400', 1996, 'Warner Books', 30, 1, 10),
(22, 'Colleen Hoover', 13.50, 'It Ends with Us', 5, 'A powerful story about love, courage, and breaking the cycle.', 0.15, 1, 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=400', 2016, 'Atria Books', 28, 1, 10),
(23, 'Emily Henry', 12.75, 'Beach Read', 5, 'Two writers challenge each other to swap genres and find unexpected romance.', 0.00, 0, 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=400', 2020, 'Berkley', 21, 1, 10),

-- Fantasy (3 books)
(24, 'J.R.R. Tolkien', 18.99, 'The Lord of the Rings', 6, 'The epic fantasy trilogy following Frodo''s quest to destroy the One Ring.', 0.00, 1, 'https://images.unsplash.com/photo-1519682337058-a94d519337bc?w=400', 1954, 'Allen & Unwin', 25, 1, 10),
(25, 'J.K. Rowling', 16.50, 'Harry Potter and the Sorcerer''s Stone', 6, 'A young wizard discovers his magical heritage and attends Hogwarts.', 0.20, 1, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400', 1997, 'Bloomsbury', 40, 1, 10),
(26, 'Brandon Sanderson', 17.25, 'Mistborn: The Final Empire', 6, 'A heist story in a world where magic comes from metals.', 0.00, 0, 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=400', 2006, 'Tor Books', 19, 1, 10),

-- Biography (2 books)
(27, 'Walter Isaacson', 19.99, 'Steve Jobs', 7, 'The exclusive biography of Apple''s legendary co-founder.', 0.00, 1, 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400', 2011, 'Simon & Schuster', 15, 1, 10),
(28, 'Ron Chernow', 21.50, 'Alexander Hamilton', 7, 'The definitive biography of one of America''s founding fathers.', 0.10, 0, 'https://images.unsplash.com/photo-1592496431122-2349e0fbc666?w=400', 2004, 'Penguin Press', 12, 1, 10),

-- Self-Help (1 book)
(29, 'Dale Carnegie', 14.99, 'How to Win Friends and Influence People', 8, 'The timeless classic on interpersonal skills and success.', 0.00, 1, 'https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=400', 1936, 'Simon & Schuster', 27, 1, 10),

-- Technology (1 book)
(30, 'Robert C. Martin', 22.99, 'Clean Code', 10, 'A handbook of agile software craftsmanship for professional developers.', 0.00, 1, 'https://images.unsplash.com/photo-1534414899046-ac2a5e0d5b5d?w=400', 2008, 'Prentice Hall', 20, 1, 10);